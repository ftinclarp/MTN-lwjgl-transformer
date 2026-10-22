package com.github.ftinclarp.lwjgl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * CLI entry point for MTN-lwjgl-transformer.
 *
 * <p>Usage: {@code java -jar MTN-lwjgl-transformer.jar <file-or-dir>...}
 *
 * <p>Each input Java source is transformed in place (GL11 → PoseStack / removed)
 * and the rewritten file overwritten. Directories are walked recursively for
 * {@code *.java}. A summary line reports rewritten vs removed per file.
 */
public final class Main {

    private static int totalRewritten;
    private static int totalRemoved;

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("usage: MTN-lwjgl-transformer <file-or-dir>...");
            System.exit(2);
        }
        for (String arg : args) {
            Path path = Paths.get(arg);
            if (Files.isDirectory(path)) {
                try (var walk = Files.walk(path)) {
                    walk.filter(p -> Files.isRegularFile(p)
                            && p.getFileName().toString().endsWith(".java"))
                        .forEach(Main::transformFile);
                }
            } else if (Files.isRegularFile(path)) {
                transformFile(path);
            } else {
                System.err.println("skip (not found): " + path);
            }
        }
        System.out.println("[MTN-LWJGL] total rewritten=" + totalRewritten
                + " removed=" + totalRemoved);
        if (totalRewritten + totalRemoved == 0) {
            System.exit(3); // nothing to do
        }
    }

    private static void transformFile(Path file) {
        try {
            String source = Files.readString(file, StandardCharsets.UTF_8);
            Gl11ToPoseStackTransformer t = new Gl11ToPoseStackTransformer();
            String out = t.transform(source);
            if (!out.equals(source)) {
                Files.writeString(file, out, StandardCharsets.UTF_8);
                totalRewritten += t.rewritten();
                totalRemoved += t.removed();
                System.out.println("[MTN-LWJGL] " + file + " (rewritten="
                        + t.rewritten() + " removed=" + t.removed() + ")");
            }
        } catch (Exception e) {
            System.err.println("[MTN-LWJGL] FAILED " + file + ": " + e);
        }
    }
}

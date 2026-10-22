package com.github.ftinclarp.lwjgl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Gl11ToPoseStackTransformerTest {

    private final Gl11ToPoseStackTransformer t = new Gl11ToPoseStackTransformer();

    @Test
    void removesGlColorAndStaticImports() {
        String src = """
                import static org.lwjgl.opengl.GL11.glColor4f;
                import static org.lwjgl.opengl.GL11.glPushMatrix;
                class R {
                    void render() {
                        glPushMatrix();
                        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    }
                }
                """;
        String out = t.transform(src);
        assertFalse(out.contains("org.lwjgl"), "static imports must be removed");
        assertTrue(out.contains("matrices.push()"));
        assertFalse(out.contains("glColor4f"));
    }

    @Test
    void rewritesTranslateScaleRotate() {
        String src = """
                class R {
                    void render() {
                        glTranslatef(0.5F, 0.5F, 0.5F);
                        glScalef(1.0F, -1.0F, -1.0F);
                        glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                        glPopMatrix();
                    }
                }
                """;
        String out = t.transform(src);
        assertTrue(out.contains("matrices.translate(0.5F, 0.5F, 0.5F)"));
        assertTrue(out.contains("matrices.scale(1.0F, -1.0F, -1.0F)"));
        assertTrue(out.contains("matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F))"));
        assertTrue(out.contains("matrices.pop()"));
        assertFalse(out.contains("glRot"));
    }

    @Test
    void removesEnableDisable() {
        String src = """
                class R {
                    void render() {
                        glEnable(32826);
                        glDisable(2896);
                    }
                }
                """;
        String out = t.transform(src);
        assertFalse(out.contains("glEnable"));
        assertFalse(out.contains("glDisable"));
    }

    @Test
    void handlesAxisFromComponents() {
        String src = """
                class R {
                    void render() {
                        glRotatef(30.0F, 1.0F, 0.0F, 0.0F);
                    }
                }
                """;
        String out = t.transform(src);
        assertTrue(out.contains("matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F))"));
    }
}

plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.2.2"
}

group = "com.github.ftinclarp"
version = "0.1.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("com.github.javaparser:javaparser-core:3.26.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveBaseName = "MTN-lwjgl-transformer"
    archiveClassifier = "all"
    mergeServiceFiles()
    manifest {
        attributes["Main-Class"] = "com.github.ftinclarp.lwjgl.Main"
        attributes["Implementation-Title"] = "MTN-lwjgl-transformer"
        attributes["Implementation-Version"] = version
    }
}

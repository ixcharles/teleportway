plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta13"
}

group = "com.umnirium.mc.teleportway"
version = "0.2.0"

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    compileOnly("org.xerial:sqlite-jdbc:3.49.1.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    shadowJar {
        relocate("org.sqlite", "com.umnirium.mc.teleportway.sqlite")

        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    build {
        dependsOn(shadowJar)
    }
}

tasks.withType<JavaExec> {
    jvmArgs("-Dpaperclip.patchonly=true")
}
plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "2.3.1"
    `maven-publish`
}

group = "b.bplugins.prefixplugin"
version = "1.1.1"
description = "BPREFIX - Paper Plugin fuer Prefix-Auswahl per GUI-Menue"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.lucko.me/")
    maven("https://repo.opencollab.dev/main/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.7")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("org.geysermc.floodgate:api:2.2.3-SNAPSHOT")
}

java {
    // Wenn wir auf JitPack sind, erzwingen wir Java 21 über die Source/Target-Kompatibilität,
    // anstatt dass Gradle krampfhaft eine Java 21 Toolchain sucht, die der Container nicht hat:
    val isJitPack = System.getenv("JITPACK") == "true"
    if (isJitPack) {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    } else {
        toolchain.languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    runServer {
        minecraftVersion("1.21")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf(
            "version" to version.toString(),
            "description" to (project.description ?: "")
        )
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
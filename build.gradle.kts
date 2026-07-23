plugins {
    java
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "b.bplugins.prefixplugin"
version = "1.0.2"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.lucko.me/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.7")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("org.jetbrains:annotations:24.0.1")
}

tasks {
    runServer {
        minecraftVersion("1.21")
    }
}

// Fix: Paper 1.21 (api-version '1.21') läuft auf Java 21, nicht Java 25.
// Mit targetJavaVersion 25 kompiliertes Bytecode wird von der Server-JVM
// mit "UnsupportedClassVersionError" abgelehnt, sobald der Server nicht
// zufällig auch auf Java 25 läuft.
val targetJavaVersion = 21
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}
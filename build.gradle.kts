plugins {
    kotlin("jvm") version "2.4.21"
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

group = "com.github.sirokuri_"
version = "2.0.0"

val minecraftVersion = "1.21.11"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        isTransitive = false
    }
    implementation(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(21)
}

tasks {
    processResources {
        filteringCharset = "UTF-8"
        val props = mapOf("version" to project.version, "apiVersion" to minecraftVersion)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    jar {
        archiveClassifier.set("plain")
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("kotlin", "com.github.sirokuri_.sellmmgui.libs.kotlin")
        dependencies {
            exclude(dependency("org.jetbrains:annotations"))
        }
        minimize()
    }

    build {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion(minecraftVersion)
    }
}

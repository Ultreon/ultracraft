import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.GradleTask
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

plugins {
    id("idea")
    id("java")
    id("groovy")
    id("java-library")
    id("scala")
}

apply(plugin = "org.jetbrains.gradle.plugin.idea-ext")

group = "io.github.ultreon.craftmods"
version = "0.1-SNAPSHOT"

base {
    archivesName.set("testmod")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    api(project(":api"))
    api(project(":client"))
    api(project(":server"))
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create("mavenScala", MavenPublication::class) {
            //noinspection GrUnresolvedAccess
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "CoreLibsGitHub"
            url = uri("https://maven.pkg.github.com/Ultreon/ultracraft")
            credentials {
                username = (project.findProperty("gpr.user") ?: System.getenv("USERNAME")) as String
                password = (project.findProperty("gpr.key") ?: System.getenv("TOKEN")) as String
            }
        }
    }
}

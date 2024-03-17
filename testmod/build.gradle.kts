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
}

apply(plugin = "org.jetbrains.gradle.plugin.idea-ext")

group = "com.github.Ultreon.craftmods"
version = "0.1.0"

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
    api(project(":desktop"))
    api(project(":server"))
}

tasks.test {
    useJUnitPlatform()
}

fun setupIdea() {
    mkdir("$projectDir/build/gameutils")
    mkdir("$projectDir/run")
    mkdir("$projectDir/run/client")
    mkdir("$projectDir/run/client/alt")
    mkdir("$projectDir/run/client/main")
    mkdir("$projectDir/run/server")

    val ps = File.pathSeparator!!
    val files = configurations["runtimeClasspath"]!!

    val classPath = files.asSequence()
        .filter { it != null }
        .map { it.path }
        .joinToString(ps)

    //language=TEXT
    val conf = """
commonProperties
	fabric.development=true
	log4j2.formatMsgNoLookups=true
	fabric.log.disableAnsi=false
	log4j.configurationFile=$projectDir/log4j.xml
    """.trimIndent()
    val launchFile = file("$projectDir/build/gameutils/launch.cfg")
    Files.writeString(
        launchFile.toPath(),
        conf,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE
    )

    val cpFile = file("$projectDir/build/gameutils/classpath.txt")
    Files.writeString(
        cpFile.toPath(),
        classPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE
    )

    idea {
        project {
            settings {
                withIDEADir {
                    println("Callback 1 executed with: $absolutePath")
                }

                runConfigurations {
                    create(
                        "Ultracraft Client",
                        Application::class.java
                    ) {                       // Create new run configuration "MyApp" that will run class foo.App
                        jvmArgs =
                            "-Xmx4g -Dfabric.skipMcProvider=true -Dfabric.dli.config=${launchFile.path} -Dfabric.dli.env=CLIENT -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient -Dfabric.zipfs.use_temp_file=false"
                        mainClass = "net.fabricmc.devlaunchinjector.Main"
                        moduleName = idea.module.name + ".desktop.main"
                        workingDirectory = "$projectDir/run/client/main/"
                        programParameters = "--gameDir=."
                        beforeRun {
                            create("Clear Quilt Cache", GradleTask::class.java) {
                                this.task = tasks.named("clearClientMainQuiltCache").get()
                            }
                        }
                    }
                    create(
                        "Ultracraft Client Alt",
                        Application::class.java
                    ) {                       // Create new run configuration "MyApp" that will run class foo.App
                        jvmArgs =
                            "-Xmx4g -Dfabric.skipMcProvider=true -Dfabric.dli.config=${launchFile.path} -Dfabric.dli.env=CLIENT -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient -Dfabric.zipfs.use_temp_file=false"
                        mainClass = "net.fabricmc.devlaunchinjector.Main"
                        moduleName = idea.module.name + ".desktop.main"
                        workingDirectory = "$projectDir/run/client/alt/"
                        programParameters = "--gameDir=."
                        beforeRun {
                            create("Clear Quilt Cache", GradleTask::class.java) {
                                this.task = tasks.named("clearClientAltQuiltCache").get()
                            }
                        }
                    }
                    create(
                        "Ultracraft Server",
                        Application::class.java
                    ) {                       // Create new run configuration "MyApp" that will run class foo.App
                        jvmArgs =
                            "-Xmx4g -Dfabric.skipMcProvider=true -Dfabric.dli.config=${launchFile.path} -Dfabric.dli.env=SERVER -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient -Dfabric.zipfs.use_temp_file=false"
                        mainClass = "net.fabricmc.devlaunchinjector.Main"
                        moduleName = idea.module.name + ".server.main"
                        workingDirectory = "$projectDir/run/server/"
                        programParameters = "--gameDir=."
                        beforeRun {
                            create("Clear Quilt Cache", GradleTask::class.java) {
                                this.task = tasks.named("clearServerQuiltCache").get()
                            }
                        }
                    }
                }
            }
        }
    }
    idea {
        module {
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }
}

import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.GradleTask
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

plugins {
    id("java")
    id("idea")
    id("dev.clojurephant.clojure") version "0.8.0-beta.7"
}

repositories {
    mavenCentral()

    maven("https://repo.clojars.org/") {
        name = "Clojars"
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    api(project(":api"))
    api(project(":client"))
    api(project(":server"))

    // requires a version of Clojure with prepl (1.10+)
    implementation("org.clojure:clojure:1.11.1")
    // and any other dependencies you want on the compile classpath
    // implementation 'group:artifact:version'

    // needed for test integration
    testRuntimeOnly("dev.clojurephant:jovial:0.4.2")
    // and any other test-specific dependencies
    // testImplementation 'group:artifact:version'

    // dependencies for REPL use only
    devImplementation("org.clojure:tools.namespace:1.3.0")
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

    rootProject.idea {
        project {
            settings {
                withIDEADir {
                    println("Callback 1 executed with: $absolutePath")
                }

                runConfigurations {
                    create(
                        "Ultracraft Client Clojure",
                        Application::class.java
                    ) {                       // Create new run configuration "MyApp" that will run class foo.App
                        jvmArgs =
                            "-Xmx4g -Dfabric.skipMcProvider=true -Dfabric.dli.config=${launchFile.path} -Dfabric.dli.env=CLIENT -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient -Dfabric.zipfs.use_temp_file=false"
                        mainClass = "net.fabricmc.devlaunchinjector.Main"
                        moduleName = idea.module.name + ".api-clojure.main"
                        workingDirectory = "$projectDir/run/client/main/"
                        programParameters = "--gameDir=."
                        beforeRun {
                            create("Clear Quilt Cache", GradleTask::class.java) {
                                this.task = tasks.named("clearClientMainQuiltCache").get()
                            }
                        }
                    }
                    create(
                        "Ultracraft Client Clojure Alt",
                        Application::class.java
                    ) {                       // Create new run configuration "MyApp" that will run class foo.App
                        jvmArgs =
                            "-Xmx4g -Dfabric.skipMcProvider=true -Dfabric.dli.config=${launchFile.path} -Dfabric.dli.env=CLIENT -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient -Dfabric.zipfs.use_temp_file=false"
                        mainClass = "net.fabricmc.devlaunchinjector.Main"
                        moduleName = idea.module.name + ".api-clojure.main"
                        workingDirectory = "$projectDir/run/client/alt/"
                        programParameters = "--gameDir=."
                        beforeRun {
                            create("Clear Quilt Cache", GradleTask::class.java) {
                                this.task = tasks.named("clearClientAltQuiltCache").get()
                            }
                        }
                    }
                    create(
                        "Ultracraft Server Clojure",
                        Application::class.java
                    ) {                       // Create new run configuration "MyApp" that will run class foo.App
                        jvmArgs =
                            "-Xmx4g -Dfabric.skipMcProvider=true -Dfabric.dli.config=${launchFile.path} -Dfabric.dli.env=SERVER -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient -Dfabric.zipfs.use_temp_file=false"
                        mainClass = "net.fabricmc.devlaunchinjector.Main"
                        moduleName = idea.module.name + ".api-clojure.main"
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
    rootProject.idea {
        module {
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }
}

beforeEvaluate {
    setupIdea()
}

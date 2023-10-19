import com.ultreon.craftutils.CraftUtilsExt
import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.GradleTask
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import java.nio.file.Files
import java.nio.file.StandardOpenOption

//file:noinspection GroovyUnusedCatchParameter
buildscript {
    repositories {
        mavenCentral()

        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            name = "sonatype"
        }

        maven {
            url = uri("https://maven.atlassian.com/3rdparty/")
        }

        maven {
            url = uri("https://storage.googleapis.com/r8-releases/raw")
        }

        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2")
        classpath("gradle.plugin.org.jetbrains.gradle.plugin.idea-ext:gradle-idea-ext:1.1.7")
    }
}

//*****************//
//     Plugins     //
//*****************//
plugins {
    id("idea")
}

apply(plugin = "java")
apply(plugin = "java-library")
apply(plugin = "org.jetbrains.gradle.plugin.idea-ext")
apply(plugin = "craftutils")

//****************************//
// Setting up main properties //
//****************************//
extensions.configure<CraftUtilsExt> {
    projectName = "Ultracraft"
    projectVersion = "0.1.0"
    projectGroup = "com.ultreon.craft"
    projectId = "ultracraft"
    production = true

    coreProject = project(":core")
    desktopProject = project(":desktop")
}

//**********************//
//     Repositories     //
//**********************//
repositories {
    mavenCentral()
    mavenLocal()
    google()
    maven("https://maven.atlassian.com/3rdparty/")
    maven("https://repo1.maven.org/maven2/")
    maven("https://repo.runelite.net/")
    maven("https://jitpack.io")
    flatDir {
        name = "Project Libraries"
        dirs = setOf(file("${projectDir}/libs"))
    }
}

/*****************
 * Configurations
 */
beforeEvaluate {
    configurations {
        // configuration that holds jars to include in the jar
        getByName("implementation") {
            isCanBeResolved = true
        }
        create("include") {
            isCanBeResolved = true
        }
        create("addToJar") {
            isCanBeResolved = true
        }
    }

    /***************
     * Dependencies
     */
    dependencies {
        // Projects
        configurations["implementation"](project(":core"))
        configurations["implementation"](project(":desktop"))
    }
}

allprojects {
    apply(plugin = "maven-publish")

    ext.also {
        it["app_name"] = "Ultreon Craft"
        it["gdx_version"] = "1.11.0"
        it["robo_vm_version"] = "2.3.16"
        it["box_2d_lights_version"] = "1.5"
        it["ashley_version"] = "1.7.4"
        it["ai_version"] = "1.8.2"
        it["gdx_controllers_version"] = "2.2.3"
    }
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven("https://jitpack.io")
        maven("https://repo1.maven.org/maven2/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.quiltmc.org/repository/release/")
        flatDir {
            name = "Project Libraries"
            dirs = setOf(file("${projectDir}/libs"))
        }
        flatDir {
            name = "Project Libraries"
            dirs = setOf(file("${rootProject.projectDir}/libs"))
        }
    }

    dependencies {

    }
}

tasks.withType(ProcessResources::class.java).configureEach {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType(Jar::class.java).configureEach {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType(JavaCompile::class.java).configureEach {
    options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation
}
println("Java: " + System.getProperty("java.version") + " JVM: " + System.getProperty("java.vm.version') + '(' + System.getProperty('java.vendor') + ') Arch: ' + System.getProperty('os.arch"))
println("OS: " + System.getProperty("os.name") + " Version: " + System.getProperty("os.version"))

println("Current version: $version")
println("Project: $group:$name")

fun setupIdea() {
    mkdir("$projectDir/build/craftutils")

    val ps = System.getProperty("path.separator")!!
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
    val launchFile = file("$projectDir/build/craftutils/launch.cfg")
    Files.writeString(
        launchFile.toPath(),
        conf,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE
    )

    val cpFile = file("$projectDir/build/craftutils/classpath.txt")
    Files.writeString(
        cpFile.toPath(),
        classPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE
    )

    evaluationDependsOnChildren()

    idea {
        project {
            settings {
                withIDEADir {
                    println("Callback 1 executed with: $absolutePath")
                }

                runConfigurations {
                    create(
                        "Ultracraft",
                        Application::class.java
                    ) {                       // Create new run configuration "MyApp" that will run class foo.App
                        jvmArgs =
                            "-Xmx2g -Dloader.skipMcProvider=true -Dfabric.dli.config=${launchFile.path} -Dfabric.dli.env=CLIENT -Dfabric.dli.main=org.quiltmc.loader.impl.launch.knot.KnotClient"
                        mainClass = "net.fabricmc.devlaunchinjector.Main"
                        moduleName = idea.module.name + ".desktop.main"
                        workingDirectory = "$projectDir/run/"
                        programParameters = "--gameDir=."
                        beforeRun {
                            create("Clear Quilt Cache", GradleTask::class.java) {
                                this.task = tasks.named("clearQuiltCache").get()
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

this.setupIdea()

import com.ultreon.gameutils.GameUtilsExt
import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.GradleTask
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import java.lang.System.getenv
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        classpath("gradle.plugin.org.danilopianini:javadoc.io-linker:0.1.4-700fdb6")
        classpath("com.android.tools.build:gradle:8.1.2")
        classpath("gradle.plugin.org.jetbrains.gradle.plugin.idea-ext:gradle-idea-ext:1.1.7")
    }
}

//*****************//
//     Plugins     //
//*****************//
plugins {
    id("idea")
    id("maven-publish")
    id("io.freefair.javadoc-links") version "8.3"
}

apply(plugin = "java")
apply(plugin = "java-library")
apply(plugin = "org.jetbrains.gradle.plugin.idea-ext")
apply(plugin = "gameutils")

//val gameVersion = "0.1.0"
val gameVersion = "0.1.0+snapshot." + DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm").format(LocalDateTime.now())
val ghBuildNumber: String? = getenv("GH_BUILD_NUMBER")

//********************************//
// Setting up the main properties //
//********************************//
extensions.configure<GameUtilsExt> {
    projectName = "Ultracraft"

    projectVersion =
        if (ghBuildNumber != null) "$gameVersion+build.$ghBuildNumber"
        else gameVersion
    projectGroup = "io.github.ultreon.ultracraft"
    projectId = "ultracraft"
    production = true

    coreProject = project(":client")
    desktopProject = project(":desktop")
    packageProject = project(":desktop-merge")

    mainClass = "com.ultreon.craft.premain.PreMain"
    javaVersion = 17
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
    maven("https://jitpack.io") {
        content {
            includeGroup("com.github.Ultreon")
            includeGroup("com.github.mgsx-dev.gdx-gltf")
            includeGroup("com.github.JnCrMx")
            includeGroup("com.github.jagrosh")
            includeGroup("space.earlygrey")
        }
    }
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
        configurations["implementation"](project(":client"))
        configurations["implementation"](project(":desktop"))
        configurations["implementation"](project(":gameprovider"))
    }
}

allprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "java")
    apply(plugin = "java-library")

    ext.also {
        it["app_name"] = "Ultracraft"
        it["gdx_version"] = property("gdx_version")
        it["robo_vm_version"] = "2.3.16"
        it["box_2d_lights_version"] = "1.5"
        it["ashley_version"] = "1.7.4"
        it["ai_version"] = "1.8.2"
        it["gdx_controllers_version"] = "2.2.3"
    }

    artifacts {
        add("archives", tasks.getByName<Jar>("jar"))
    }

    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven("https://repo1.maven.org/maven2/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.quiltmc.org/repository/release/")
        maven("https://oss.sonatype.org/content/repositories/releases")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://jitpack.io")

        maven("https://raw.githubusercontent.com/Ultreon/corelibs/main/.mvnrepo/")
        maven("https://raw.githubusercontent.com/Ultreon/ultreon-data/main/.mvnrepo/")

        maven {
            name = "CoreLibsGitHub"
            url = uri("https://maven.pkg.github.com/Ultreon/corelibs")

            credentials {
                username = (project.findProperty("gpr.user") ?: getenv("USERNAME")) as String?
                password = (project.findProperty("gpr.key") ?: getenv("TOKEN")) as String?
            }
        }

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

subprojects {
    extensions.getByType(BasePluginExtension::class.java).apply {
        this.archivesName.set(property("project_name").toString() + "-" + property("project_version"))
    }

    tasks.withType(JavaCompile::class.java).configureEach {
        options.encoding = "UTF-8"
    }

    tasks.withType(Jar::class.java).configureEach {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        archiveBaseName.set("ultracraft-${project.name}")
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

println("Current version: $gameVersion")
println("Project: $group:$name")

afterEvaluate {
    tasks.getByName("javadoc", Javadoc::class) {
        source(subprojects.map { subproject ->
            subproject?.extensions?.getByType(JavaPluginExtension::class.java)?.sourceSets?.getByName("main")?.allJava?.sourceDirectories
        })
        this.title = "Ultracraft API"
        this.setDestinationDir(File(rootProject.projectDir, "/build/docs/javadoc"))
        // Configure the classpath
        classpath = files(subprojects.map { subproject ->
            subproject?.extensions?.getByType(JavaPluginExtension::class.java)?.sourceSets?.getByName("main")?.compileClasspath
        })
        val options = options as StandardJavadocDocletOptions
        options.addFileOption("-add-stylesheet", project.file("javadoc.css"))
        options.links(
            "https://javadoc.io/doc/com.badlogicgames.gdx/gdx/${project.property("gdx_version")}",
            "https://javadoc.io/doc/com.badlogicgames.gdx/gdx-ai/${project.property("ai_version")}",
            "https://javadoc.io/doc/com.badlogicgames.gdx/gdx-backend-lwjgl3/${project.property("gdx_version")}",
            "https://javadoc.io/doc/io.netty/netty-buffer/${project.property("netty_version")}",
            "https://javadoc.io/doc/io.netty/netty-codec-socks/${project.property("netty_version")}",
            "https://javadoc.io/doc/io.netty/netty-common/${project.property("netty_version")}",
            "https://javadoc.io/doc/io.netty/netty-handler/${project.property("netty_version")}",
            "https://javadoc.io/doc/io.netty/netty-resolver/${project.property("netty_version")}",
            "https://javadoc.io/doc/io.netty/netty-transport/${project.property("netty_version")}",
            "https://javadoc.io/doc/io.netty/netty-transport-classes-epoll/${project.property("netty_version")}",
            "https://javadoc.io/doc/it.unimi.dsi/fastutil/8.5.12",
            "https://javadoc.io/doc/it.unimi.dsi/fastutil-core/8.5.9",
            "https://javadoc.io/doc/net.java.dev.jna/jna/${project.property("jna_version")}",
            "https://javadoc.io/doc/org.apache.logging.log4j/log4j-api/${project.property("log4j_version")}",
            "https://javadoc.io/doc/org.apache.logging.log4j/log4j-core/${project.property("log4j_version")}",
            "https://javadoc.io/doc/org.apache.commons/commons-collections4/${project.property("commons_collections4_version")}",
            "https://javadoc.io/doc/org.apache.commons/commons-compress/${project.property("commons_compress_version")}",
            "https://javadoc.io/doc/org.apache.commons/commons-lang3/${project.property("commons_lang3_version")}",
            "https://javadoc.io/doc/org.slf4j/slf4j-api/${project.property("slf4j_version_javadoc")}",
            "https://ultreon.github.io/corelibs/docs/${project.property("corelibs_version")}",
            "https://ultreon.github.io/ultreon-data/docs/${project.property("ultreon_data_version")}",
            "https://maven.fabricmc.net/docs/fabric-loader-${project.property("fabric_version")}",
        )
    }

    this.apply(plugin = "org.danilopianini.javadoc.io-linker")
}

artifacts {

}

val publishProjects =
    listOf(project(":client"), project(":desktop"), project(":server"), project(":gameprovider"))

publishProjects.forEach {
    it.artifacts {
        this.add("archives", tasks.named<Jar>("jar"))
    }

    it.publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(it.components["java"])

                groupId = "io.github.ultreon.craft"
                artifactId = "ultracraft-${it.name}"
                version = gameVersion

                pom {
                    this@pom.name.set("Ultracraft")
                    this@pom.description.set("Ultracraft is a voxel game that focuses on technology based survival.")

                    this@pom.url.set("https://github.com/Ultreon/ultracraft")
                    this@pom.inceptionYear.set("2023")

                    this@pom.developers {
                        developer {
                            id.set("XyperCode")
                            name.set("XyperCode")

                            organization.set("Ultreon")
                            organizationUrl.set("https://ultreon.github.io/")

                            roles.set(listOf("Owner", "Maintainer"))

                            url.set("https://github.com/Ultreon/ultracraft")
                        }
                    }

                    this@pom.organization {
                        name.set("Ultreon")
                        url.set("https://ultreon.github.io/")
                    }

                    this@pom.issueManagement {
                        system.set("GitHub")
                        url.set("https://github.com/Ultreon/ultracraft/issues")
                    }

                    this@pom.scm {
                        url.set("https://github.com/Ultreon/ultracraft")
                        connection.set("scm:git:git://github.com/Ultreon/ultracraft.git")
                    }

                    this@pom.licenses {
                        license {
                            name.set("AGPL-3.0")
                            url.set("https://github.com/Ultreon/ultracraft/blob/main/LICENSE")
                        }
                    }

                    this@pom.contributors {
                        contributor {
                            name.set("XyperCode")
                            url.set("https://github.com/XyperCode")

                            roles.set(listOf("Author", "Developer"))
                        }

                        contributor {
                            name.set("MaroonShaded")
                            url.set("https://github.com/MaroonShaded")

                            roles.set(listOf("Contributor"))
                        }

                        contributor {
                            name.set("Oszoou")
                            url.set("https://github.com/Oszoou")

                            roles.set(listOf("Contributor"))
                        }

                        contributor {
                            name.set("MincraftEinstein")
                            url.set("https://github.com/MincraftEinstein")

                            roles.set(listOf("Tester", "Modeler"))
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                name = "UltracraftGitHub"
                url = uri("https://maven.pkg.github.com/Ultreon/ultracraft")
                credentials {
                    username = (project.findProperty("gpr.user") ?: getenv("USERNAME")) as String?
                    password = (project.findProperty("gpr.key") ?: getenv("TOKEN")) as String?
                }
            }
        }
    }
}

publishing {
    publications {
        create("maven", MavenPublication::class) {
            from(components["java"])

            groupId = "io.github.ultreon.craft"
            artifactId = "ultracraft"
            version = gameVersion

            pom {
                this@pom.name.set("Ultracraft")
                this@pom.description.set("Ultracraft is a voxel game that focuses on technology based survival.")

                this@pom.url.set("https://github.com/Ultreon/ultracraft")
                this@pom.inceptionYear.set("2023")

                this@pom.developers {
                    developer {
                        id.set("XyperCode")
                        name.set("XyperCode")

                        organization.set("Ultreon")
                        organizationUrl.set("https://ultreon.github.io/")

                        roles.set(listOf("Owner", "Maintainer"))

                        url.set("https://github.com/Ultreon/ultracraft")
                    }
                }

                this@pom.organization {
                    name.set("Ultreon")
                    url.set("https://ultreon.github.io/")
                }

                this@pom.issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/Ultreon/ultracraft/issues")
                }

                this@pom.scm {
                    url.set("https://github.com/Ultreon/ultracraft")
                    connection.set("scm:git:git://github.com/Ultreon/ultracraft.git")
                }

                this@pom.licenses {
                    license {
                        name.set("AGPL-3.0")
                        url.set("https://github.com/Ultreon/ultracraft/blob/main/LICENSE")
                    }
                }

                this@pom.contributors {
                    contributor {
                        name.set("XyperCode")
                        url.set("https://github.com/XyperCode")

                        roles.set(listOf("Author", "Developer"))
                    }

                    contributor {
                        name.set("MaroonShaded")
                        url.set("https://github.com/MaroonShaded")

                        roles.set(listOf("Contributor"))
                    }

                    contributor {
                        name.set("Oszoou")
                        url.set("https://github.com/Oszoou")

                        roles.set(listOf("Contributor"))
                    }

                    contributor {
                        name.set("MincraftEinstein")
                        url.set("https://github.com/MincraftEinstein")

                        roles.set(listOf("Tester", "Modeler"))
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "UltracraftGitHub"
            url = uri("https://maven.pkg.github.com/Ultreon/ultracraft")
            credentials {
                username = (project.findProperty("gpr.user") ?: getenv("USERNAME")) as String?
                password = (project.findProperty("gpr.key") ?: getenv("TOKEN")) as String?
            }
        }
    }
}

idea {
    project {
        settings {
            fun setupIdea(dependency: Project, name: String) {

                mkdir("${projectDir}/build/gameutils")
                mkdir("${projectDir}/run")
                mkdir("${projectDir}/run/client")
                mkdir("${projectDir}/run/client/alt")
                mkdir("${projectDir}/run/client/main")
                mkdir("${projectDir}/run/server")

                val ps = File.pathSeparator!!
                val files = project.configurations["runtimeClasspath"]!!

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
	log4j.configurationFile=${project.projectDir}/log4j.xml
    """.trimIndent()
                val launchFile = file("${project.projectDir}/build/gameutils/launch.cfg")
                Files.writeString(
                    launchFile.toPath(),
                    conf,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
                )

                val cpFile = file("${project.projectDir}/build/gameutils/classpath.txt")
                Files.writeString(
                    cpFile.toPath(),
                    classPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
                )

                withIDEADir {
                    println("Callback 1 executed with: $absolutePath")
                }

                runConfigurations {
                    create(
                        "Ultracraft Client $name",
                        Application::class.java
                    ) {                       // Create new run configuration "MyApp" that will run class foo.App
                        jvmArgs =
                            "-Xmx4g -Dfabric.skipMcProvider=true -Dfabric.dli.config=${launchFile.path} -Dfabric.dli.env=CLIENT -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient -Dfabric.zipfs.use_temp_file=false"
                        mainClass = "net.fabricmc.devlaunchinjector.Main"
                        moduleName = idea.module.name + ".${project.name.substring(1)}.main"
                        workingDirectory = "$projectDir/run/client/main/"
                        programParameters = "--gameDir=."
                    }
                    create(
                        "Ultracraft Client $name Alt",
                        Application::class.java
                    ) {                       // Create new run configuration "MyApp" that will run class foo.App
                        jvmArgs =
                            "-Xmx4g -Dfabric.skipMcProvider=true -Dfabric.dli.config=${launchFile.path} -Dfabric.dli.env=CLIENT -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient -Dfabric.zipfs.use_temp_file=false"
                        mainClass = "net.fabricmc.devlaunchinjector.Main"
                        moduleName = idea.module.name + ".${project.name.substring(1)}.main"
                        workingDirectory = "$projectDir/run/client/alt/"
                        programParameters = "--gameDir=."
                    }
                    create(
                        "Ultracraft Server $name",
                        Application::class.java
                    ) {                       // Create new run configuration "MyApp" that will run class foo.App
                        jvmArgs =
                            "-Xmx4g -Dfabric.skipMcProvider=true -Dfabric.dli.config=${launchFile.path} -Dfabric.dli.env=SERVER -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient -Dfabric.zipfs.use_temp_file=false"
                        mainClass = "net.fabricmc.devlaunchinjector.Main"
                        moduleName = idea.module.name + ".${project.name.substring(1)}.main"
                        workingDirectory = "$projectDir/run/server/"
                        programParameters = "--gameDir=."
                    }
                }
            }

            setupIdea(rootProject.project(":desktop"), "Java")
            setupIdea(rootProject.project(":testmod"), "Test Mod")
        }
    }
}

tasks.register<Copy>("docker-jar") {
    dependsOn(":server:jar")

    from(project(":server").tasks.getByName("serverJar").outputs)
    into("$projectDir/build/docker")

    rename {
        "server.jar"
    }
}

tasks.register<DefaultTask>("docker-prepare") {
    dependsOn("docker-jar")

    doLast {
        // Prepare docker image
        val runScriptPath = file("$projectDir/build/docker/run.sh").toPath()

        val serverClassPath = project(":server").configurations["runtimeClasspath"]!!
        val classPath = serverClassPath.asSequence()
            .filter { it != null }
            .map {
                copy {
                    from(it)
                    into("$projectDir/build/docker/lib")
                }
                return@map "./lib/${it.name}"
            }
            .joinToString(":")

        val runScript = """
#!/bin/bash
java -cp ./server.jar:$classPath net.fabricmc.loader.impl.launch.knot.KnotClient
        """.trimIndent()
        if (!Files.exists(runScriptPath))
            Files.createFile(runScriptPath)

        Files.writeString(runScriptPath, runScript, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)

        copy {
            from("$projectDir/log4j.xml")
            into("$projectDir/build/docker")
        }

        copy {
            from("$projectDir/Dockerfile")
            into("$projectDir/build/docker")
        }

        Files.writeString(Paths.get("$projectDir/build/docker/image-version.txt"), gameVersion, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)
    }
}

tasks.register<DefaultTask>("docker-image") {
    dependsOn("docker-prepare")

    doLast {
        // Build docker image
        exec {
            workingDir = file("$projectDir/build/docker")
            if (!gameVersion.matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+"))) {
                commandLine("docker", "build", "-t", "${project.name}/server:latest", ".")
            } else {
                commandLine("docker", "build", "-t", "${project.name}/server:${gameVersion}", ".")
            }
        }
    }
}

tasks.register<DefaultTask>("docker-run") {
    dependsOn("docker-image")

    doLast {
        exec {
            workingDir = file("$projectDir/build/docker")
            commandLine("docker", "run", "--rm", "-i", "--name", "gradle-${project.name}", "${project.name}/server")
        }
    }
}

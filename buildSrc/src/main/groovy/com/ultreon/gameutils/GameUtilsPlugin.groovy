package com.ultreon.gameutils


import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.stream.JsonWriter
import com.ultreon.gameutils.tasks.ClearQuiltCacheTask
import com.ultreon.gameutils.tasks.MetadataTask
import com.ultreon.gameutils.tasks.PrepareRunTask
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar

@SuppressWarnings('unused')
class GameUtilsPlugin implements Plugin<Project> {
    static GameUtilsExt extension

    GameUtilsPlugin() {

    }


    @Override
    void apply(Project project) {
        if (project != project.rootProject) return

        extension = project.extensions.create("gameutils", GameUtilsExt.class)
        extension.runDirectory = project.file("run")

        project.afterEvaluate {
            if (!extension.production) {
                println("WARNING: App $extension.projectName is in developer mode.")
            }

            if (extension.javaVersion == -1) {
                throw new GradleException("Java Version is not set.")
            }
            if (extension.packageProject == null) {
                throw new GradleException("Project to package is not set.")
            }
            if (extension.mainClass == null) {
                throw new GradleException("Main class is not set.")
            }

            extension.packageProject.with { Project proj ->
                proj.configurations.register("pack") {
                    it.canBeResolved = true
                    it.canBeConsumed = true
                }

                def metadataTask = proj.tasks.register("metadata", MetadataTask.class)

                proj.rootProject.tasks.register("pack", Zip) { Zip zip ->
                    zip.dependsOn metadataTask

                    zip.group = "gameutils"

                    def json = new JsonObject()
                    def classpathJson = new JsonArray()

                    proj.configurations.pack.with { Configuration conf ->
                        List<Dep> dependencies = []
                        if (conf.isCanBeResolved()) {
                            conf.getResolvedConfiguration().getResolvedArtifacts().each {
                                at ->
                                    def dep = at.getModuleVersion().getId()
                                    dependencies.add(new Dep(dep.group, dep.name, dep.version, at.extension, at.classifier, at.file))
                            }
                        } else {
                            throw new GradleException("Pack config can't be resolved!")
                        }
                        dependencies.collect { Dep dep ->
                            dep.file.with { File file ->
                                String name
                                if (dep.classifier == null || dep.classifier == "null") {
                                    name = dep.name + "-" + dep.version + "." + dep.extension
                                } else {
                                    name = dep.name + "-" + dep.version + "-" + dep.classifier + "." + dep.extension
                                }
                                {
                                    def dest = "libraries/" + dep.group.replaceAll("\\.", "/") + "/" + dep.name
                                    println "Adding \"$file.name\" to \"$dest\""

                                    zip.from file, new Action<CopySpec>() {
                                        @Override
                                        void execute(CopySpec spec) {
                                            spec.into(dest)
                                        }
                                    }
                                }
                                classpathJson.add "libraries/" + dep.group.replaceAll("\\.", "/") + "/" + dep.name + "/" + name
                                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                            }

                            return null
                        }
                    }

                    json.add("classpath", classpathJson)

                    def sdkJson = new JsonObject()
                    sdkJson.addProperty("version", proj.tasks.named("compileJava", JavaCompile).get().targetCompatibility)
                    sdkJson.addProperty("type", "JavaJDK")
                    json.add("sdk", sdkJson)
                    json.addProperty("main-class", extension.mainClass)
                    json.addProperty("game", "ultracraft")

                    def gson = new GsonBuilder().create()
                    def writer = new JsonWriter(new FileWriter(proj.file("$proj.projectDir/build/config.json")))
                    gson.toJson json, writer
                    writer.flush()
                    writer.close()

                    zip.from(metadataTask.get().metadataFile)
                    zip.from("$proj.projectDir/build/config.json")
                    zip.from(tasks.jar.outputs, new Action<CopySpec>() {
                        @Override
                        void execute(CopySpec copySpec) {
                            copySpec.rename { extension.projectVersion + ".jar" }
                        }
                    })

                    println metadataTask.get().metadataFile

                    zip.destinationDirectory.set(file("$proj.projectDir/build/dist"))
                    zip.archiveBaseName.set("package")
                    proj.delete(zip.archiveFile)
                }
            }
        }

        project.allprojects.collect { Project it ->

        }

        project.subprojects.collect { Project subproject ->
            subproject.extensions.create("projectConfig", ProjectConfigExt)

            subproject.beforeEvaluate {
                def projectConfig = subproject.extensions.getByType(ProjectConfigExt)
                def jarTasks = subproject.tasks.withType(Jar).toList()
                jarTasks.collect { Jar jar ->
                    jar.archiveBaseName.set("$extension.projectId-$subproject.name")
                    jar.archiveVersion.set(extension.projectVersion)
                    jar.archiveFileName.set("$extension.projectId-$subproject.name-${extension.projectVersion}.jar")

                    for (Task dependsTask : projectConfig.jarDependTasks.get()) {
                        dependsTask.dependsOn(jar)
                    }
                }

                subproject.properties.put "app_name", extension.projectName
                subproject.version = extension.projectVersion
                subproject.group = extension.projectGroup
            }

            subproject.afterEvaluate {
                def platform = subproject.extensions.getByType(ProjectConfigExt)
                if (platform == null || platform.type == null) {
//                    throw new GradleException("Platform not set for project ${subproject.path}")
                }
            }
        }

        project.tasks.register("prepareRun", PrepareRunTask.class)
        project.tasks.register("clearServerQuiltCache", ClearQuiltCacheTask.class, "server")
        project.tasks.register("clearClientMainQuiltCache", ClearQuiltCacheTask.class, "client/main")
        project.tasks.register("clearClientAltQuiltCache", ClearQuiltCacheTask.class, "client/alt")
    }
}

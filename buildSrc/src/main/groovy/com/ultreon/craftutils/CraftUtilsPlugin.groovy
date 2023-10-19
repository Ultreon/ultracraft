package com.ultreon.craftutils

import com.ultreon.craftutils.tasks.ClearQuiltCacheTask
import com.ultreon.craftutils.tasks.PrepareRunTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.jvm.tasks.Jar

class CraftUtilsPlugin implements Plugin<Project> {
    CraftUtilsPlugin() {

    }


    @Override
    void apply(Project project) {
        if (project != project.rootProject) return

        def extension = project.extensions.create("craftutils", CraftUtilsExt.class)
        extension.runDirectory = project.file("run")

        project.afterEvaluate {
            if (!extension.production) {
                println("WARNING: App $extension.projectName is in developer mode.")
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
        project.tasks.register("clearQuiltCache", ClearQuiltCacheTask.class)
    }
}

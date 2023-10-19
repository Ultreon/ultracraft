package com.ultreon.craftutils.tasks

import com.ultreon.craftutils.CraftUtilsExt
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class ClearQuiltCacheTask extends DefaultTask {
    ClearQuiltCacheTask() {
        group = "craftutils"
    }

    @TaskAction
    void createRun() {
        def directory = project.rootProject.extensions.getByType(CraftUtilsExt).runDirectory
        project.delete {
            delete project.fileTree(directory)
        }
    }
}

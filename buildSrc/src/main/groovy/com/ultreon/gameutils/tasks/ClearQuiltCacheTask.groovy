package com.ultreon.gameutils.tasks

import com.ultreon.gameutils.GameUtilsExt
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class ClearQuiltCacheTask extends DefaultTask {
    @Input
    def directory = project.rootProject.extensions.getByType(GameUtilsExt).runDirectory

    ClearQuiltCacheTask() {
        group = "gameutils"
    }

    @TaskAction
    void createRun() {
        project.delete {
            delete project.fileTree(directory)
        }
    }
}

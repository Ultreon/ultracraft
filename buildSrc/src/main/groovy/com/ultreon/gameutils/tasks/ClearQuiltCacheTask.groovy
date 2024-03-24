package com.ultreon.gameutils.tasks

import com.ultreon.gameutils.GameUtilsExt
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

class ClearQuiltCacheTask extends DefaultTask {
    @Input
    def directory = project.rootProject.extensions.getByType(GameUtilsExt).runDirectory
    private final String name

    @Inject
    ClearQuiltCacheTask(String name) {
        this.name = name
        group = "gameutils"
    }

    @TaskAction
    void createRun() {
        project.delete(project.file(new File(directory, "$name/.cache/quilt_loader")))
    }
}

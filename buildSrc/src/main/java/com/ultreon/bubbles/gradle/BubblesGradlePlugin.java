package com.ultreon.bubbles.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.annotation.Nonnull;

public class BubblesGradlePlugin implements Plugin<Project> {
    private static BubblesGradlePlugin instance;

    public BubblesGradlePlugin() {
        instance = this;
    }

    public static BubblesGradlePlugin get() {
        return instance;
    }

    @Override
    public void apply(@Nonnull Project project) {
        project.getTasks().register("retrieveUrls", RetrieveUrlsTask.class);
        project.getTasks().register("bubbles", BubblesTask.class);

        if (project.getRootProject().equals(project)) {
            project.getTasks().register("writeDownloadJson", WriteDownloadJsonTask.class);
        }
    }
}

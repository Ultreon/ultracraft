package com.ultreon.bubbles.gradle;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Date;
import java.time.Instant;
import java.util.stream.Collectors;

public class WriteDownloadJsonTask extends BaseTask {
    public WriteDownloadJsonTask() {
        super("writeDownloadJson", "bubbles");

        var collect = this.getProject().getSubprojects().stream().map(Project::getName).collect(Collectors.toList());
        this.dependsOn("retrieveUrls");
        for (var project : collect) {
            this.dependsOn(project + ":retrieveUrls");
        }
        this.doFirst(this::execute);
    }

    private void execute(Task task) {
        var t = this.getProject().getTasks().getByName("retrieveUrls");
        var t1 = this.getProject().getTasks().getByName("bubbles");
        if (!(t instanceof RetrieveUrlsTask)) {
            throw new RuntimeException("The task 'retrieveUrls' is not the internal bubble blaster task for retrieving urls.");
        }
        var retrieveUrls = (RetrieveUrlsTask) t;

        if (!(t1 instanceof BubblesTask)) {
            throw new RuntimeException("The task 'bubbles' is not the internal bubble blaster task for retrieving urls.");
        }
        var bubbles = (BubblesTask) t1;

        var gson = new Gson();
        var sw = new StringWriter();
        var jw = new JsonWriter(sw);
        jw.setIndent("  ");
        gson.toJson(new BuildVersion(this.getProject().getVersion().toString(), null, Date.from(Instant.now()), bubbles.getVersionType(), retrieveUrls.getGameDeps()), BuildVersion.class, jw);
        var rootProject = this.getProject().getRootProject();
        try {
            Files.write(Paths.get(rootProject.getProjectDir().getPath() + "/build/libs/libraries.json"), sw.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

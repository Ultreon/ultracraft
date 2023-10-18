package com.ultreon.bubbles.gradle;

import org.gradle.api.Task;
import org.gradle.api.tasks.Internal;

public class BubblesTask extends BaseTask {
    public VersionType versionType;

    public BubblesTask() {
        super("bubbles", "bubbles");

        this.doFirst(this::execute);
    }

    private void execute(Task task) {
        if (this.versionType == null) {
            throw new IllegalArgumentException("Version type is not set.");
        }
    }

    @Internal
    public VersionType getVersionType() {
        return this.versionType;
    }

    public void setVersionType(VersionType versionType) {
        this.versionType = versionType;
    }
}

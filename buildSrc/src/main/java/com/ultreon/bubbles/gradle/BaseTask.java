 package com.ultreon.bubbles.gradle;

import org.gradle.api.DefaultTask;

public abstract class BaseTask extends DefaultTask {
    private final String name;

    public BaseTask(String name, String group) {
        this.name = name;
        this.setGroup(group);
    }
}

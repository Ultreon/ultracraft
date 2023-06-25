package com.ultreon.craft;

import com.ultreon.craft.platform.OperatingSystem;

import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AndroidPlatform extends GamePlatform {
    private final ConcurrentMap<String, Logger> loggers = new ConcurrentHashMap<>();

    @Override
    public Logger getLogger(String name) {
        return this.loggers.computeIfAbsent(name, s -> new AndroidLogger(name));
    }

    @Override
    public OperatingSystem getOperatingSystem() {
        return OperatingSystem.ANDROID;
    }
}

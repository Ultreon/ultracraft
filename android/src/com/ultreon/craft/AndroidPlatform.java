package com.ultreon.craft;

import android.content.Intent;
import android.os.Bundle;
import com.ultreon.craft.platform.OperatingSystem;

import com.ultreon.libs.crash.v0.CrashLog;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AndroidPlatform extends GamePlatform {
    private final ConcurrentMap<String, Logger> loggers = new ConcurrentHashMap<>();
    private final AndroidLauncher launcher;

    public AndroidPlatform(AndroidLauncher launcher) {

        this.launcher = launcher;
    }

    @Override
    public Logger getLogger(String name) {
        return this.loggers.computeIfAbsent(name, s -> new AndroidLogger(name));
    }

    @Override
    public OperatingSystem getOperatingSystem() {
        return OperatingSystem.ANDROID;
    }

    @Override
    public void handleCrash(CrashLog crashLog) {
        super.handleCrash(crashLog);

        Intent intent = new Intent(this.launcher, CrashActivity.class);
        intent.putExtra("CrashLog", crashLog.toString());
        this.launcher.startActivity(intent);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return BuildConfig.DEBUG;
    }
}

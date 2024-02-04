package com.ultreon.craft.client;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.ultreon.libs.crash.v0.CrashException;
import com.ultreon.libs.crash.v0.CrashLog;
import org.jetbrains.annotations.Nullable;

public class GameLibGDXWrapper implements ApplicationListener {
    private final String[] argv;
    @Nullable
    private UltracraftClient client;
    private Thread.UncaughtExceptionHandler exceptionHandler;

    public GameLibGDXWrapper(String[] argv) {
        this.argv = argv;
    }

    private void uncaughtException(Thread thread, Throwable throwable) {
        if (throwable instanceof CrashException e) {
            try {
                CrashLog crashLog = e.getCrashLog();
                UltracraftClient.get().delayCrash(crashLog);
            } catch (Throwable t) {
                this.exceptionHandler.uncaughtException(thread, t);
            }
        }
        this.exceptionHandler.uncaughtException(thread, throwable);
    }

    @Override
    public void create() {
        try {
            Gdx.app.setLogLevel(Application.LOG_DEBUG);
            this.client = new UltracraftClient(this.argv);

            this.exceptionHandler = Thread.currentThread().getUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException);

            Thread.currentThread().setUncaughtExceptionHandler(this::uncaughtException);
        } catch (CrashException t) {
            UltracraftClient.crash(t);
        }
    }

    @Override
    public void resize(int width, int height) {
        if (this.client != null) this.client.resize(width, height);
    }

    @Override
    public void render() {
        try {
            if (this.client != null) this.client.render();
        } catch (CrashException e) {
            CrashLog crashLog = e.getCrashLog();
            UltracraftClient.crash(crashLog);
        }
    }

    @Override
    public void pause() {
        try {
            if (this.client != null) this.client.pause();
        } catch (CrashException e) {
            CrashLog crashLog = e.getCrashLog();
            UltracraftClient.crash(crashLog);
        }
    }

    @Override
    public void resume() {
        try {
            if (this.client != null) this.client.resume();
        } catch (CrashException e) {
            CrashLog crashLog = e.getCrashLog();
            UltracraftClient.crash(crashLog);
        }
    }

    @Override
    public void dispose() {
        try {
            if (this.client != null) this.client.dispose();
        } catch (CrashException e) {
            CrashLog crashLog = e.getCrashLog();
            UltracraftClient.crash(crashLog);
        }
    }
}

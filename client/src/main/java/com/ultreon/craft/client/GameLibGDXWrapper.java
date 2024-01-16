package com.ultreon.craft.client;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.crash.ApplicationCrash;
import com.ultreon.craft.crash.CrashLog;
import com.ultreon.craft.util.ArgParser;
import org.jetbrains.annotations.Nullable;

public class GameLibGDXWrapper implements ApplicationListener {
    private final String[] argv;
    @Nullable
    private UltracraftClient client;
    private Thread.UncaughtExceptionHandler exceptionHandler;

    public GameLibGDXWrapper(String[] argv) {
        UltracraftClient.arguments = new ArgParser(argv);
        UltracraftClient.logDebug();
        this.argv = argv;
    }

    private void uncaughtException(Thread thread, Throwable throwable) {
        if (throwable instanceof ApplicationCrash e) {
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
            GamePlatform.get().prepare();
            Gdx.app.setLogLevel(Application.LOG_DEBUG);
            this.client = new UltracraftClient(this.argv);

            this.exceptionHandler = Thread.currentThread().getUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException);

            Thread.currentThread().setUncaughtExceptionHandler(this::uncaughtException);
        } catch (ApplicationCrash t) {
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
        } catch (ApplicationCrash e) {
            CrashLog crashLog = e.getCrashLog();
            UltracraftClient.crash(crashLog);
        }
    }

    @Override
    public void pause() {
        try {
            if (this.client != null) this.client.pause();
        } catch (ApplicationCrash e) {
            CrashLog crashLog = e.getCrashLog();
            UltracraftClient.crash(crashLog);
        }
    }

    @Override
    public void resume() {
        try {
            if (this.client != null) this.client.resume();
        } catch (ApplicationCrash e) {
            CrashLog crashLog = e.getCrashLog();
            UltracraftClient.crash(crashLog);
        }
    }

    @Override
    public void dispose() {
        try {
            if (this.client != null) this.client.dispose();
        } catch (ApplicationCrash e) {
            CrashLog crashLog = e.getCrashLog();
            UltracraftClient.crash(crashLog);
        }
    }
}

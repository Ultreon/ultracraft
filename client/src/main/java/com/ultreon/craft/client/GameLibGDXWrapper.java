package com.ultreon.craft.client;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.crash.ApplicationCrash;
import com.ultreon.craft.crash.CrashLog;
import kotlin.OptIn;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * LibGDX wrapper for Ultracraft to handle uncaught exceptions.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
@ApiStatus.Internal
@OptIn(markerClass = InternalApi.class)
public final class GameLibGDXWrapper implements ApplicationListener {
    private final String[] argv;
    @Nullable
    private UltracraftClient client;

    /**
     * Constructs a new GameLibGDXWrapper object.
     *
     * @param argv The command line arguments.
     */
    public GameLibGDXWrapper(String[] argv) {
        this.argv = argv;
    }

    /**
     * Handles uncaught exceptions.
     * If the exception is an ApplicationCrash, delays the crash log processing using UltracraftClient.
     * Logs the exception otherwise.
     *
     * @param thread    The thread where the exception occurred
     * @param throwable The uncaught exception
     */
    private void uncaughtException(Thread thread, Throwable throwable) {
        if (throwable instanceof ApplicationCrash e) {
            try {
                CrashLog crashLog = e.getCrashLog();
                UltracraftClient.get().delayCrash(crashLog);
                return;
            } catch (Throwable t) {
                CommonConstants.LOGGER.error("Failed to handle uncaught exception", t);
            }
        }
        CommonConstants.LOGGER.error("Uncaught exception", throwable);
    }

    /**
     * Initializes the UltracraftClient and sets up exception handlers.
     */
    @Override
    public void create() {
        try {
            // Set log level to debug
            Gdx.app.setLogLevel(Application.LOG_DEBUG);

            // Initialize UltracraftClient with given arguments
            this.client = new UltracraftClient(this.argv);

            // Set default uncaught exception handler
            Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException);

            // Set current thread's uncaught exception handler
            Thread.currentThread().setUncaughtExceptionHandler(this::uncaughtException);
        } catch (ApplicationCrash t) {
            // Handle ApplicationCrash exception
            UltracraftClient.crash(t);
        }
    }

    /**
     * Resizes the client if it is not null.
     *
     * @param width  the new width
     * @param height the new height
     */
    @Override
    public void resize(int width, int height) {
        if (this.client != null) {
            this.client.resize(width, height);
        }
    }

    /**
     * Renders the client if it is not null, handling any ApplicationCrash exceptions.
     */
    @Override
    public void render() {
        try {
            if (this.client != null) {
                this.client.render();
            }
        } catch (ApplicationCrash e) {
            CrashLog crashLog = e.getCrashLog();
            UltracraftClient.crash(crashLog);
        }
    }

    /**
     * Pauses the client if it is not null and handles any ApplicationCrash exceptions.
     */
    @Override
    public void pause() {
        try {
            if (this.client != null) {
                this.client.pause();
            }
        } catch (ApplicationCrash e) {
            CrashLog crashLog = e.getCrashLog();
            UltracraftClient.crash(crashLog);
        }
    }

    /**
     * Resumes the client.
     */
    @Override
    public void resume() {
        try {
            // Check if the client is not null before resuming
            if (this.client != null) {
                this.client.resume();
            }
        } catch (ApplicationCrash e) {
            // If an ApplicationCrash exception occurs, handle it by logging the crash
            CrashLog crashLog = e.getCrashLog();
            UltracraftClient.crash(crashLog);
        }
    }

    /**
     * Clean up resources and handle any potential crashes.
     */
    @Override
    public void dispose() {
        try {
            // Dispose the client if it exists
            if (this.client != null) this.client.dispose();
        } catch (ApplicationCrash e) {
            // Handle the application crash
            CrashLog crashLog = e.getCrashLog();
            UltracraftClient.crash(crashLog);
        }
    }
}

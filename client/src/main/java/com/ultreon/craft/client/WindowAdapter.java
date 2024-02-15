package com.ultreon.craft.client;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.ultreon.craft.client.api.events.WindowEvents;

class WindowAdapter extends Lwjgl3WindowAdapter {
    private Lwjgl3Window window;

    @Override
    public void created(Lwjgl3Window window) {
        Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.NOTIFICATION, false);
        Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.LOW, false);
        Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.MEDIUM, true);
        Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.HIGH, true);

        WindowEvents.WINDOW_CREATED.factory().onWindowCreated(window);
        this.window = window;
    }

    @Override
    public void focusLost() {
        UltracraftClient.get().pause();

        WindowEvents.WINDOW_FOCUS_CHANGED.factory().onWindowFocusChanged(this.window, false);
    }

    @Override
    public void focusGained() {
        WindowEvents.WINDOW_FOCUS_CHANGED.factory().onWindowFocusChanged(this.window, true);
    }

    @Override
    public boolean closeRequested() {
        if (WindowEvents.WINDOW_CLOSE_REQUESTED.factory().onWindowCloseRequested(this.window).isCanceled())
            return false;

        return UltracraftClient.get().tryShutdown();
    }

    @Override
    public void filesDropped(String[] files) {
        if (UltracraftClient.get().filesDropped(files)) return;

        WindowEvents.WINDOW_FILES_DROPPED.factory().onWindowFilesDropped(this.window, files);
    }

}

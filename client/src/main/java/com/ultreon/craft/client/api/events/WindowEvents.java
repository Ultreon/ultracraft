package com.ultreon.craft.client.api.events;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.events.api.EventResult;

public class WindowEvents {
    public static final Event<WindowCreated> WINDOW_CREATED = Event.create();
    public static final Event<WindowResized> WINDOW_RESIZED = Event.create();
    public static final Event<WindowMoved> WINDOW_MOVED = Event.create();
    public static final Event<WindowFocusChanged> WINDOW_FOCUS_CHANGED = Event.create();
    public static final Event<WindowCloseRequested> WINDOW_CLOSE_REQUESTED = Event.withResult();
    public static final Event<WindowFilesDropped> WINDOW_FILES_DROPPED = Event.create();

    @FunctionalInterface
    public interface WindowCreated {
        void onWindowCreated(Lwjgl3Window window);
    }

    @FunctionalInterface
    public interface WindowResized {
        void onWindowResized(Lwjgl3Window window, int width, int height);
    }

    @FunctionalInterface
    public interface WindowMoved {
        void onWindowMoved(Lwjgl3Window window, int x, int y);
    }

    @FunctionalInterface
    public interface WindowFocusChanged {
        void onWindowFocusChanged(Lwjgl3Window window, boolean focused);
    }

    @FunctionalInterface
    public interface WindowCloseRequested {
        EventResult onWindowCloseRequested(Lwjgl3Window window);
    }

    @FunctionalInterface
    public interface WindowFilesDropped {
        void onWindowFilesDropped(Lwjgl3Window window, String[] files);
    }
}

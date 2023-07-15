package com.ultreon.craft.events;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.libs.events.v1.Event;
import com.ultreon.libs.events.v1.EventResult;

public class LifecycleEvents {
    public static final Event<GameLoaded> GAME_LOADED = Event.create();
    public static final Event<GameDisposed> GAME_DISPOSED = Event.create();
    public static final Event<WindowClosed> WINDOW_CLOSED = Event.withResult();

    @FunctionalInterface
    public interface GameLoaded {
        void onGameLoaded(UltreonCraft game);
    }

    @FunctionalInterface
    public interface GameDisposed {
        void onGameDisposed();
    }

    @FunctionalInterface
    public interface WindowClosed {
        EventResult onWindowClose();
    }
}

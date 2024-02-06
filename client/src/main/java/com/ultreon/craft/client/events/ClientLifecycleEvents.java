package com.ultreon.craft.client.events;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.events.api.EventResult;

public class ClientLifecycleEvents {
    public static final Event<GameLoaded> GAME_LOADED = Event.create();
    public static final Event<GameDisposed> GAME_DISPOSED = Event.create();
    public static final Event<WindowClosed> WINDOW_CLOSED = Event.withResult();
    public static final Event<Registration> REGISTER_MODELS = Event.create();
    public static final Event<Registration> REGISTER_ENTITY_RENDERERS = Event.create();
    public static final Event<Registration> REGISTER_BLOCK_RENDERERS = Event.create();
    public static final Event<Registration> REGISTER_BLOCK_RENDER_TYPES = Event.create();
    public static final Event<Registration> REGISTER_BLOCK_ENTITY_MODELS = Event.create();
    public static final Event<Registration> REGISTER_BLOCK_MODELS = Event.create();

    @FunctionalInterface
    public interface GameLoaded {
        void onGameLoaded(UltracraftClient client);
    }

    @FunctionalInterface
    public interface GameDisposed {
        void onGameDisposed();
    }

    @FunctionalInterface
    public interface WindowClosed {
        EventResult onWindowClose();
    }

    @FunctionalInterface
    public interface Registration {
        void onRegister();
    }
}

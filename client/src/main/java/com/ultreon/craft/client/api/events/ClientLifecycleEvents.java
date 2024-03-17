package com.ultreon.craft.client.api.events;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.events.api.EventResult;

public class ClientLifecycleEvents {
    public static final Event<ClientStarted> CLIENT_STARTED = Event.create();
    @Deprecated(forRemoval = true, since = "0.1.0")
    public static final Event<ClientStarted> GAME_LOADED = CLIENT_STARTED;
    public static final Event<ClientStopped> CLIENT_STOPPED = Event.create();
    @Deprecated
    public static final Event<ClientStopped> GAME_DISPOSED = CLIENT_STOPPED;
    public static final Event<WindowClosed> WINDOW_CLOSED = Event.create();
    @Deprecated(forRemoval = true, since = "0.1.0")
    public static final Event<Registration> REGISTER_ENTITY_MODELS = Event.create();
    @Deprecated(forRemoval = true, since = "0.1.0")
    public static final Event<Registration> REGISTER_ENTITY_RENDERERS = Event.create();
    @Deprecated(forRemoval = true, since = "0.1.0")
    public static final Event<Registration> REGISTER_BLOCK_RENDERERS = Event.create();
    @Deprecated(forRemoval = true, since = "0.1.0")
    public static final Event<Registration> REGISTER_BLOCK_RENDER_TYPES = Event.create();
    @Deprecated(forRemoval = true, since = "0.1.0")
    public static final Event<Registration> REGISTER_BLOCK_ENTITY_MODELS = Event.create();
    @Deprecated(forRemoval = true, since = "0.1.0")
    public static final Event<Registration> REGISTER_BLOCK_MODELS = Event.create();

    @FunctionalInterface
    public interface ClientStarted {
        void onGameLoaded(UltracraftClient client);
    }

    @FunctionalInterface
    public interface ClientStopped {
        void onGameDisposed();
    }

    @FunctionalInterface
    public interface WindowClosed {
        void onWindowClose();
    }

    @Deprecated(forRemoval = true, since = "0.1.0")
    @FunctionalInterface
    public interface Registration {
        void onRegister();
    }
}

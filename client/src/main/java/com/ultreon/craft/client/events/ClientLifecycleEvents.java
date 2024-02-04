package com.ultreon.craft.client.events;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.libs.events.v1.Event;
import com.ultreon.libs.events.v1.EventResult;

public class ClientLifecycleEvents {
    public static final Event<GameLoaded> GAME_LOADED = Event.create();
    public static final Event<GameDisposed> GAME_DISPOSED = Event.create();
    public static final Event<WindowClosed> WINDOW_CLOSED = Event.withResult();
    public static final Event<RegisterModels> REGISTER_MODELS = Event.create();
    public static final Event<RegisterRenderers> REGISTER_ENTITY_RENDERERS = Event.create();
    public static final Event<RegisterBlockRenderTypes> REGiSTER_BLOCK_RENDER_TYPES = Event.create();

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
    public interface RegisterModels {
        void onRegisterModels();
    }

    @FunctionalInterface
    public interface RegisterRenderers {
        void onRegisterEntityRenderers();
    }

    @FunctionalInterface
    public interface RegisterBlockRenderTypes {
        void onRegisterBlockRenderTypes();
    }
}

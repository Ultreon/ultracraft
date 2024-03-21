package com.ultreon.craft.client.api.events;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.events.api.Event;

public class ClientReloadEvent {
    public static final Event<SkinLoaded> SKIN_LOADED = Event.create();
    public static final Event<SkinReload> SKIN_RELOAD = Event.create();

    @FunctionalInterface
    public interface SkinLoaded {
        void onSkinLoaded(Texture texture, Pixmap pixmap);
    }

    @FunctionalInterface
    public interface SkinReload {
        void onSkinReload();
    }
}

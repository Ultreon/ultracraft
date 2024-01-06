package com.ultreon.craft.client.events;

import com.ultreon.craft.client.world.ClientChunk;
import com.ultreon.craft.events.api.Event;

public class ClientChunkEvents {
    public static final Event<Render> REBUILT = Event.create();

    @FunctionalInterface
    public interface Render {
        void onClientChunkRebuilt(ClientChunk chunk);
    }
}

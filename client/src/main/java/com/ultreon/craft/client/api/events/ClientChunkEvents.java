package com.ultreon.craft.client.api.events;

import com.ultreon.craft.client.world.ClientChunk;
import com.ultreon.craft.events.api.Event;

public class ClientChunkEvents {
    public static final Event<Received> RECEIVED = Event.create();
    public static final Event<Rebuilt> REBUILT = Event.create();
    public static final Event<Rebuilt> BUILT = Event.create();

    @FunctionalInterface
    public interface Received {
        void onClientChunkReceived(ClientChunk chunk);
    }

    @FunctionalInterface
    public interface Rebuilt {
        void onClientChunkRebuilt(ClientChunk chunk);
    }

    @FunctionalInterface
    public interface Built {
        void onClientChunkBuilt(ClientChunk chunk);
    }
}

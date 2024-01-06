package com.ultreon.craft.server.events;

import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.server.UltracraftServer;

public class ServerLifecycleEvents {
    public static final Event<ServerStarting> SERVER_STARTING = Event.create();
    public static final Event<ServerStarted> SERVER_STARTED = Event.create();
    public static final Event<ServerStopping> SERVER_STOPPING = Event.create();
    public static final Event<ServerStopped> SERVER_STOPPED = Event.create();

    @FunctionalInterface
    public interface ServerStarting {
        void onServerStarting(UltracraftServer server);
    }

    @FunctionalInterface
    public interface ServerStarted {
        void onServerStarted(UltracraftServer server);
    }
    
    @FunctionalInterface
    public interface ServerStopping {
        void onServerStopping(UltracraftServer server);
    }

    @FunctionalInterface
    public interface ServerStopped {
        void onServerStopped(UltracraftServer server);
    }
}

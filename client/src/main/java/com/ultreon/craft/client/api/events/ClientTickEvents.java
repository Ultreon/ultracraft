package com.ultreon.craft.client.api.events;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.player.ClientPlayer;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.events.api.EventResult;

public class ClientTickEvents {
    public static final Event<PreGameTick> PRE_GAME_TICK = Event.withResult();
    public static final Event<PostGameTick> POST_GAME_TICK = Event.create();
    public static final Event<PrePlayerTick> PRE_PLAYER_TICK = Event.withResult();
    public static final Event<PostPlayerTick> POST_PLAYER_TICK = Event.create();
    public static final Event<PreWorldTick> PRE_WORLD_TICK = Event.withResult();
    public static final Event<PostWorldTick> POST_WORLD_TICK = Event.create();

    @FunctionalInterface
    public interface PreGameTick {
        EventResult onGameTick(UltracraftClient client);
    }

    @FunctionalInterface
    public interface PostGameTick {
        void onGameTick(UltracraftClient client);
    }

    @FunctionalInterface
    public interface PrePlayerTick {
        EventResult onPlayerTick(ClientPlayer player);
    }

    @FunctionalInterface
    public interface PostPlayerTick {
        void onPlayerTick(ClientPlayer player);
    }

    @FunctionalInterface
    public interface PreWorldTick {
        EventResult onWorldTick(ClientWorld world);
    }

    @FunctionalInterface
    public interface PostWorldTick {
        void onWorldTick(ClientWorld world);
    }
}

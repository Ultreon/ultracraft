package com.ultreon.craft.client.events;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.player.ClientPlayer;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.libs.events.v1.Event;

public class ClientTickEvents {
    public static final Event<GameTick> GAME_TICK = Event.create();
    public static final Event<PlayerTick> PLAYER_TICK = Event.create();
    public static final Event<WorldTick> WORLD_TICK = Event.create();

    @FunctionalInterface
    public interface GameTick {
        void onGameTick(UltracraftClient client);
    }

    @FunctionalInterface
    public interface PlayerTick {
        void onPlayerTick(ClientPlayer player);
    }

    @FunctionalInterface
    public interface WorldTick {
        void onWorldTick(ClientWorld world);
    }
}

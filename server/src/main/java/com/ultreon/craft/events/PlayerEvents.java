package com.ultreon.craft.events;

import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.events.api.EventResult;
import com.ultreon.craft.menu.Inventory;
import com.ultreon.craft.server.player.ServerPlayer;

public class PlayerEvents {
    public static final Event<InitialItems> INITIAL_ITEMS = Event.withResult();
    public static final Event<Joined> PLAYER_JOINED = Event.create();
    public static final Event<Left> PLAYER_LEFT = Event.create();
    public static final Event<Spawned> PLAYER_SPAWNED = Event.create();

    @FunctionalInterface
    public interface InitialItems {
        EventResult onPlayerInitialItems(ServerPlayer player, Inventory inventory);
    }

    @FunctionalInterface
    public interface Joined {
        void onPlayerJoined(ServerPlayer player);
    }

    @FunctionalInterface
    public interface Left {
        void onPlayerLeft(ServerPlayer player);
    }

    @FunctionalInterface
    public interface Spawned {
        void onPlayerSpawned(ServerPlayer player);
    }
}

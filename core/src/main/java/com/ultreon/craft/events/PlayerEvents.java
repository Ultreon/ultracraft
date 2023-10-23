package com.ultreon.craft.events;

import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.menu.Inventory;
import com.ultreon.libs.events.v1.Event;
import com.ultreon.libs.events.v1.EventResult;

import java.util.List;

public class PlayerEvents {
    public static final Event<InitialItems> INITIAL_ITEMS = Event.withResult();

    @FunctionalInterface
    public interface InitialItems {
        EventResult onPlayerInitialItems(Player player, Inventory inventory);
    }
}

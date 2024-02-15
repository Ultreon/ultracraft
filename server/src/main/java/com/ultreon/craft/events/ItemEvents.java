package com.ultreon.craft.events;

import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.UseItemContext;
import org.jetbrains.annotations.ApiStatus;

public class ItemEvents {
    public static final Event<Use> USE = Event.create();

    @ApiStatus.Experimental
    public static final Event<Dropped> DROPPED = Event.create();

    @FunctionalInterface
    public interface Use {
        void onUseItem(Item item, UseItemContext context);
    }

    @FunctionalInterface
    @ApiStatus.Experimental
    public interface Dropped {
        void onDropped(ItemStack item);
    }
}

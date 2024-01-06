package com.ultreon.craft.events;

import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.UseItemContext;

public class ItemEvents {
    public static final Event<Use> USE = Event.create();

    @FunctionalInterface
    public interface Use {
        void onUseItem(Item item, UseItemContext context);
    }
}

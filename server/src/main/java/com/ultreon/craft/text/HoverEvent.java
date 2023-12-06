package com.ultreon.craft.text;

import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.world.Location;

import java.io.Serializable;

public record HoverEvent<T>(HoverEvent.Action action, T value) implements Serializable {
    public static HoverEvent<TextObject> text(String text) {
        return new HoverEvent<>(Action.TEXT, TextObject.literal(text));
    }
    public static HoverEvent<TextObject> text(TextObject text) {
        return new HoverEvent<>(Action.TEXT, text);
    }

    public static HoverEvent<ItemStack> item(ItemStack stack) {
        return new HoverEvent<>(Action.TEXT, stack);
    }

    public static HoverEvent<Location> location(Location stack) {
        return new HoverEvent<>(Action.TEXT, stack);
    }

    public static HoverEvent<Player> location(Player stack) {
        return new HoverEvent<>(Action.TEXT, stack);
    }

    public static HoverEvent<Entity> location(Entity stack) {
        return new HoverEvent<>(Action.TEXT, stack);
    }

    public enum Action {
        TEXT,
        ITEM,
        LOCATION,
        PLAYER,
        ENTITY,
    }
}

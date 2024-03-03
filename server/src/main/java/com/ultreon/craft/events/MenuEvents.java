package com.ultreon.craft.events;

import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.events.api.EventResult;
import com.ultreon.craft.menu.ContainerMenu;
import com.ultreon.craft.menu.ItemSlot;
import com.ultreon.craft.server.player.ServerPlayer;

public class MenuEvents {
    public static final Event<MenuClickEvent> MENU_CLICK = Event.withResult();
    public static final Event<MenuCloseEvent> MENU_CLOSE = Event.create();
    public static final Event<MenuOpenEvent> MENU_OPEN = Event.withResult();

    @FunctionalInterface
    public interface MenuClickEvent {
        EventResult onMenuClick(ContainerMenu menu, ServerPlayer player, ItemSlot slot, boolean rightClick);
    }

    @FunctionalInterface
    public interface MenuCloseEvent {
        void onMenuClose(ContainerMenu menu, ServerPlayer player);
    }

    @FunctionalInterface
    public interface MenuOpenEvent {
        EventResult onMenuOpen(ContainerMenu menu, ServerPlayer player);
    }
}

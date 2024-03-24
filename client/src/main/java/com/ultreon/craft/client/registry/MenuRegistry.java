package com.ultreon.craft.client.registry;

import com.ultreon.craft.client.gui.screens.container.ContainerScreen;
import com.ultreon.craft.menu.ContainerMenu;
import com.ultreon.craft.menu.MenuType;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class MenuRegistry {
    private static final Map<Identifier, ScreenBuilder<?>> REGISTRY = new HashMap<>();

    public static <T extends ContainerMenu> void registerScreen(MenuType<T> menu, ScreenBuilder<T> builder) {
        MenuRegistry.REGISTRY.put(menu.getId(), builder);
    }

    public static ContainerScreen getScreen(ContainerMenu menu) {
        return MenuRegistry.getScreen0(menu);
    }

    @SuppressWarnings("unchecked")
    private static <T extends ContainerMenu> ContainerScreen getScreen0(T menu) {
        ScreenBuilder<T> builder = (ScreenBuilder<T>) MenuRegistry.REGISTRY.get(menu.getType().getId());
        if (builder == null) return null;
        return builder.build(menu, menu.getTitle());
    }

    public interface ScreenBuilder<T extends ContainerMenu> {
        ContainerScreen build(T menu, TextObject title);
    }
}

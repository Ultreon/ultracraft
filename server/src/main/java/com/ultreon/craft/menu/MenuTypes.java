package com.ultreon.craft.menu;

import com.ultreon.craft.entity.Player;
import com.ultreon.craft.registry.Registries;
import com.ultreon.libs.commons.v0.Identifier;

public class MenuTypes {
    public static final MenuType<Inventory> INVENTORY = MenuTypes.register("inventory", (type, world, entity, pos) -> {
        if (entity instanceof Player player) return player.inventory;
        else return null;
    });

    private static <T extends ContainerMenu> MenuType<T> register(String name, MenuType.MenuBuilder<T> menuBuilder) {
        MenuType<T> menuType = new MenuType<>(menuBuilder);
        Registries.MENU_TYPES.register(new Identifier(name), menuType);
        return menuType;
    }
}

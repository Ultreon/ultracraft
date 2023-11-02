package com.ultreon.craft.menu;

import com.ultreon.craft.registry.Registries;
import com.ultreon.libs.commons.v0.Identifier;

public class MenuTypes {
    public static final MenuType<Inventory> INVENTORY = MenuTypes.register("inventory", Inventory::new);

    private static <T extends ContainerMenu> MenuType<T> register(String name, MenuType.MenuBuilder<T> menuBuilder) {
        MenuType<T> menuType = new MenuType<>(menuBuilder);
        Registries.MENU_TYPES.register(new Identifier(name), menuType);
        return menuType;
    }
}

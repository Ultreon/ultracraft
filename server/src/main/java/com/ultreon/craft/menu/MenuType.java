package com.ultreon.craft.menu;

import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.Nullable;

public class MenuType<T extends ContainerMenu> {
    private final MenuBuilder<T> menuBuilder;

    public MenuType(MenuBuilder<T> menuBuilder) {
        this.menuBuilder = menuBuilder;
    }

    public @Nullable T create(World world, Entity entity, @Nullable BlockPos pos) {
        return this.menuBuilder.create(this, world, entity, pos);
    }

    public ElementID getId() {
        return Registries.MENU_TYPE.getId(this);
    }

    public interface MenuBuilder<T extends ContainerMenu> {
        @Nullable T create(MenuType<T> menuType, World world, Entity entity, @Nullable BlockPos pos);
    }
}

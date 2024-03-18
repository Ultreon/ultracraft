package com.ultreon.craft.menu;

import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ItemContainerMenu extends EntityContainerMenu {
    private final ItemStack
            holder;

    /**
     * Creates a new {@link EntityContainerMenu}
     *
     * @param type   the type of the menu.
     * @param world  the world where the menu is opened in.
     * @param entity the entity that opened the menu.
     * @param owner  the owner of the menu.
     * @param holder the item that holds the items in the menu.
     * @param pos    the position where the menu is opened.
     * @param size   the number of slots.
     */
    protected ItemContainerMenu(@NotNull MenuType<?> type, @NotNull World world, @NotNull Entity entity, @NotNull Entity owner, ItemStack holder, @Nullable BlockPos pos, int size) {
        super(type, world, entity, owner, pos, size);
        this.holder = holder;
    }

    public ItemStack getHolder() {
        return holder;
    }
}

package com.ultreon.craft.menu;

import com.ultreon.craft.block.entity.BlockEntity;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BlockContainerMenu extends ContainerMenu {
    private final BlockEntity blockEntity;

    /**
     * Creates a new {@link BlockContainerMenu}
     *
     * @param type        the type of the menu.
     * @param world       the world where the menu is opened in.
     * @param entity      the entity that opened the menu.
     * @param blockEntity the block entity where the menu is opened in.
     * @param pos         the position where the menu is opened.
     * @param size        the number of slots.
     */
    protected BlockContainerMenu(@NotNull MenuType<?> type, @NotNull World world, @NotNull Entity entity, @Nullable BlockEntity blockEntity, @Nullable BlockPos pos, int size) {
        super(type, world, entity, pos, size);
        this.blockEntity = blockEntity;
    }

    public @Nullable BlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}

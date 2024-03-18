package com.ultreon.craft.block.entity;

import com.ultreon.craft.entity.Player;
import com.ultreon.craft.menu.CrateMenu;
import com.ultreon.craft.menu.MenuTypes;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;

public class CrateBlockEntity extends ContainerBlockEntity<CrateMenu> {
    public static final int ITEM_CAPACITY = 27;

    public CrateBlockEntity(BlockEntityType<?> type, World world, BlockPos pos) {
        super(type, world, pos, ITEM_CAPACITY);
    }

    @Override
    public @NotNull CrateMenu createMenu(Player player) {
        return new CrateMenu(MenuTypes.CRATE, this.world, player, this, this.pos);
    }
}

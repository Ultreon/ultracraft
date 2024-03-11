package com.ultreon.craft.block;

import com.ultreon.craft.block.state.BlockDataEntry;
import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.CubicDirection;
import com.ultreon.craft.world.World;

public class BlastFurnaceBlock extends Block {
    public BlastFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockMetadata createMeta() {
        return super.createMeta().withEntry("lit", BlockDataEntry.of(false)).withEntry("facing", BlockDataEntry.ofEnum(CubicDirection.NORTH));
    }

    @Override
    public BlockMetadata onPlacedBy(World world, BlockPos blockPos, BlockMetadata blockMeta, Player player, ItemStack stack, CubicDirection direction) {
        System.out.println("On placed by " + player.getName() + " at " + blockPos + " facing " + direction);

        return blockMeta.withEntry("facing", BlockDataEntry.ofEnum(direction));
    }

    @Override
    public void onPlace(World world, BlockPos pos, BlockMetadata blockMetadata) {
        super.onPlace(world, pos, blockMetadata);
    }
}

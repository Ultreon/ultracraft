package com.ultreon.craft.block;

import com.ultreon.craft.block.entity.BlockEntity;
import com.ultreon.craft.block.entity.BlockEntityTypes;
import com.ultreon.craft.block.entity.CrateBlockEntity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.UseResult;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;

public class CrateBlock extends EntityBlock {
    public CrateBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull BlockEntity createBlockEntity(World world, BlockPos pos) {
        return BlockEntityTypes.CRATE.create(world, pos);
    }

    @Override
    public UseResult use(@NotNull World world, @NotNull Player player, @NotNull Item item, @NotNull BlockPos pos) {
        super.use(world, player, item, pos);

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CrateBlockEntity crate && world.isClientSide()) {
            crate.open(player);
            return UseResult.ALLOW;
        }

        return UseResult.SKIP;
    }
}

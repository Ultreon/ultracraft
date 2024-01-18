package com.ultreon.craft.block;

import com.ultreon.craft.block.entity.BlockEntity;
import com.ultreon.craft.block.entity.BlockEntityTypes;
import com.ultreon.craft.block.entity.CrateBlockEntity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.InteractResult;
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
    public InteractResult use(@NotNull World world, @NotNull Player player, @NotNull BlockPos pos) {
        super.use(world, player, pos);

        if (world.isClientSide()) return InteractResult.ALLOW;

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CrateBlockEntity crate) {
            crate.open(player);
        }

        return InteractResult.ALLOW;
    }
}

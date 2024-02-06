package com.ultreon.craft.block;

import com.ultreon.craft.block.entity.BlockEntity;
import com.ultreon.craft.block.entity.BlockEntityType;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;

public abstract class EntityBlock extends Block {
    public EntityBlock() {
    }

    public EntityBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(World world, BlockPos pos) {
        super.onPlace(world, pos);

        world.setBlockEntity(pos, this.createBlockEntity(world, pos));
    }

    @NotNull
    protected abstract BlockEntity createBlockEntity(World world, BlockPos pos);

    public static EntityBlock simple(BlockEntityType<?> type) {
        return new EntityBlock() {
            @NotNull
            @Override
            protected BlockEntity createBlockEntity(World world, BlockPos pos) {
                return type.create(world, pos);
            }
        };
    }

    public static EntityBlock simple(BlockEntityType<?> type, Properties properties) {
        return new EntityBlock(properties) {
            @NotNull
            @Override
            protected BlockEntity createBlockEntity(World world, BlockPos pos) {
                return type.create(world, pos);
            }
        };
    }
}

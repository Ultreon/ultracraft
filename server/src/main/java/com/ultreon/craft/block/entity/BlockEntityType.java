package com.ultreon.craft.block.entity;

import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.Nullable;

public class BlockEntityType<T extends BlockEntity> {
    private final BlockEntityFactory<T> factory;

    public BlockEntityType(BlockEntityFactory<T> factory) {
        this.factory = factory;
    }

    public T create(World world, BlockPos pos) {
        return factory.create(this, world, pos);
    }

    public T load(World world, BlockPos pos, MapType data) {
        T blockEntity = factory.create(this, world, pos);
        blockEntity.load(data);
        return blockEntity;
    }

    public @Nullable ElementID getId() {
        return Registries.BLOCK_ENTITY_TYPE.getId(this);
    }

    public int getRawId() {
        return Registries.BLOCK_ENTITY_TYPE.getRawId(this);
    }

    @FunctionalInterface
    public interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockEntityType<T> type, World world, BlockPos pos);
    }
}

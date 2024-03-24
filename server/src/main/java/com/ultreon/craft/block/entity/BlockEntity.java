package com.ultreon.craft.block.entity;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.MapType;

import java.util.Objects;

public abstract class BlockEntity {
    private final BlockEntityType<?> type;
    protected final World world;
    protected final BlockPos pos;

    public BlockEntity(BlockEntityType<?> type, World world, BlockPos pos) {
        this.type = type;
        this.world = world;
        this.pos = pos;
    }

    public Block getBlock() {
        return world.get(pos);
    }

    public World getWorld() {
        return world;
    }

    public BlockPos pos() {
        return pos;
    }

    public BlockEntityType<?> getType() {
        return type;
    }

    public static BlockEntity fullyLoad(World world, BlockPos pos, MapType mapType) {
        Identifier type = Identifier.tryParse(mapType.getString("type"));
        BlockEntityType<?> value = Registries.BLOCK_ENTITY_TYPE.getElement(type);
        return value.load(world, pos, mapType);
    }

    public void load(MapType data) {

    }

    public MapType save(MapType data) {
        data.putString("type", Objects.requireNonNull(type.getId()).toString());
        return data;
    }
}

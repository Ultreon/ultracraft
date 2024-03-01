package com.ultreon.craft.world;

import com.ultreon.craft.block.Block;
import com.ultreon.libs.commons.v0.vector.Vec3i;

public interface ChunkAccess {

    boolean setFast(int x, int y, int z, Block block);

    boolean set(int x, int y, int z, Block block);

    default boolean set(BlockPos pos, Block block) {
        return this.set(pos.x(), pos.y(), pos.z(), block);
    }

    default boolean set(Vec3i pos, Block block) {
        return this.set(pos.x, pos.y, pos.z, block);
    }

    Block getFast(int x, int y, int z);

    Block get(int x, int y, int z);

    default Block get(BlockPos pos) {
        return this.get(pos.x(), pos.y(), pos.z());
    }

    default Block get(Vec3i pos) {
        return this.get(pos.x, pos.y, pos.z);
    }

    Vec3i getOffset();

    int getHighest(int x, int z);
}

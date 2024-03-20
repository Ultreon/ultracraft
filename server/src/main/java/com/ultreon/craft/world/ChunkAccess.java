package com.ultreon.craft.world;

import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.libs.commons.v0.vector.Vec3i;

public interface ChunkAccess {

    boolean setFast(int x, int y, int z, BlockMetadata block);

    boolean set(int x, int y, int z, BlockMetadata block);

    BlockMetadata getFast(int x, int y, int z);

    BlockMetadata get(int x, int y, int z);

    Vec3i getOffset();

    int getHighest(int x, int z);
}

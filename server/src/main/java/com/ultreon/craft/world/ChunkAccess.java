package com.ultreon.craft.world;

import com.ultreon.craft.block.Block;
import com.ultreon.libs.commons.v0.vector.Vec3i;

public interface ChunkAccess {

    boolean setFast(int x, int y, int z, Block block);

    boolean set(int x, int y, int z, Block block);

    Block getFast(int x, int y, int z);

    Block get(int x, int y, int z);

    Vec3i getOffset();

    int getHighest(int x, int z);
}

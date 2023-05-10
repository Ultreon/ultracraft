package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.world.Chunk;

public abstract class TerrainLayer {
    public abstract boolean handle(Chunk chunk, int x, int y, int z, int height, long seed);
}

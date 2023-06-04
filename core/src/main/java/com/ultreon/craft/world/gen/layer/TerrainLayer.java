package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.world.RawChunk;

public abstract class TerrainLayer {
    public abstract boolean handle(RawChunk chunk, int x, int y, int z, int height, long seed);
}

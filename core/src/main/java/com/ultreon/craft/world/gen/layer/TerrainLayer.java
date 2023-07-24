package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.world.RawChunk;
import com.ultreon.craft.world.World;

public abstract class TerrainLayer {
    public abstract boolean handle(World world, RawChunk chunk, int x, int y, int z, int height, long seed);
}

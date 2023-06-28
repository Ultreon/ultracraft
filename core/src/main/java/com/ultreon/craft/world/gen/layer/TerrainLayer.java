package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;

public abstract class TerrainLayer {
    public abstract boolean handle(World world, Chunk chunk, int x, int y, int z, int height);
}

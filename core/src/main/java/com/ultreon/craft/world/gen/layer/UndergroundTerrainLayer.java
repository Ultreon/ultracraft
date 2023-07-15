package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;

public class UndergroundTerrainLayer extends TerrainLayer {
    @Override
    public boolean handle(World world, Chunk chunk, int x, int y, int z, int height) {
        if (y < height) {
            chunk.set(x, y, z, Blocks.DIRT);
            return true;
        }
        return false;
    }
}

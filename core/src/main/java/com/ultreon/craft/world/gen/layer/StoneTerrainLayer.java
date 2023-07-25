package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;

public class StoneTerrainLayer extends TerrainLayer {
    @Override
    public boolean handle(World world, Chunk chunk, int x, int y, int z, int height, long seed) {
        if (y < height - 3) {
            chunk.set(x, y, z, Blocks.STONE);
            return true;
        }
        return false;
    }
}

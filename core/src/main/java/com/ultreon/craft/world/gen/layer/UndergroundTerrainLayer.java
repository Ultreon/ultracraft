package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.RawChunk;

public class UndergroundTerrainLayer extends TerrainLayer {
    @Override
    public boolean handle(RawChunk chunk, int x, int y, int z, int height, long seed) {
        if (y < height) {
            chunk.set(x, y, z, Blocks.DIRT);
            return true;
        }
        return false;
    }
}

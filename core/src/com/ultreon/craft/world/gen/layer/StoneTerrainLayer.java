package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.util.Vec2i;
import com.ultreon.craft.world.Chunk;

public class StoneTerrainLayer extends TerrainLayer {
    @Override
    public boolean handle(Chunk chunk, int x, int y, int z, int height, Vec2i mapSeed) {
        if (y < height - 3) {
            chunk.set(x, y, z, Blocks.STONE.get());
            return true;
        }
        return false;
    }
}

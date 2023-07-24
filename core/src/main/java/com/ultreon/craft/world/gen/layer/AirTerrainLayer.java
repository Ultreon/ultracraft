package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.RawChunk;
import com.ultreon.craft.world.World;

public class AirTerrainLayer extends TerrainLayer {
    @Override
    public boolean handle(World world, RawChunk chunk, int x, int y, int z, int height) {
        if (y > height) {
            chunk.set(x, y, z, Blocks.AIR);
            return true;
        }
        return false;
    }
}

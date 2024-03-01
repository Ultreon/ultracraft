package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;

public class AirTerrainLayer extends TerrainLayer {
    @Override
    public Block handle(World world, Chunk chunk, int x, int y, int z, int height) {
        return y > height ? Blocks.AIR : null;
    }
}

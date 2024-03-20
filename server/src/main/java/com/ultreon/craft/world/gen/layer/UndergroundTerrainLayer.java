package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;

public class UndergroundTerrainLayer extends TerrainLayer {
    private final Block block;
    private final int offset;

    public UndergroundTerrainLayer(Block block, int offset) {
        this.block = block;
        this.offset = offset;
    }

    @Override
    public boolean handle(World world, Chunk chunk, int x, int y, int z, int height) {
        if (y <= height - offset) {
            chunk.set(x, y, z, block.createMeta());
            return true;
        }
        return false;
    }
}

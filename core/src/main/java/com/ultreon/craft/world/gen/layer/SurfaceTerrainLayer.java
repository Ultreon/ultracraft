package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.debug.Debugger;
import com.ultreon.craft.world.Chunk;

public class SurfaceTerrainLayer extends TerrainLayer {
    private final Block material;

    public SurfaceTerrainLayer(Block material) {
        this.material = material;
    }

    @Override
    public boolean handle(Chunk chunk, int x, int y, int z, int height, long seed) {
        if (y == height) {
            chunk.set(x, y, z, material);
            return true;
        }
        return false;
    }
}

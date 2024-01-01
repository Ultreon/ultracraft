package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;

public class VoidGuardTerrainLayer extends TerrainLayer {
    public VoidGuardTerrainLayer() {

    }

    @Override
    public boolean handle(World world, Chunk chunk, int x, int y, int z, int height) {
        if (y == 70) {
            chunk.set(x, y, z, Blocks.VOIDGUARD);
            return true;
        }
        return false;
    }
}

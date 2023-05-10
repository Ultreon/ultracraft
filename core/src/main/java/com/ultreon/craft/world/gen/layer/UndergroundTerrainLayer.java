package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.debug.Debugger;
import com.ultreon.craft.world.Chunk;

public class UndergroundTerrainLayer extends TerrainLayer {
    @Override
    public boolean handle(Chunk chunk, int x, int y, int z, int height, long seed) {
        Debugger.layersTriggered.add(this);
        if (y < height) {
            chunk.set(x, y, z, Blocks.DIRT);
            Debugger.layersHandled.add(this);
            return true;
        }
        return false;
    }
}

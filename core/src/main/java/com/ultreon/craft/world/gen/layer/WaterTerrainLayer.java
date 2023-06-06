package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;

public class WaterTerrainLayer extends TerrainLayer {
    private final int waterLevel;

    public WaterTerrainLayer() {
        this(64);
    }

    public WaterTerrainLayer(int waterLevel) {
        this.waterLevel = waterLevel;
    }

    @Override
    public boolean handle(World world, Chunk chunk, int x, int y, int z, int height) {
        if (y > height && y <= waterLevel) {
            chunk.set(x, y, z, Blocks.WATER);
            if (y == height + 1) {
                chunk.set(x, height, z, Blocks.SAND);
            }
            return true;
        }
        return false;
    }
}

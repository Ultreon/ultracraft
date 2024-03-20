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
        // Set water layer from height up to water level y
        if (y <= this.waterLevel + 1 && y > height) {
            chunk.set(x, y, z, Blocks.WATER.createMeta());
            return true;
        }

        // Set sand layer from the height - 3 up to water level + 2
        if (y <= this.waterLevel + 2 && y <= height && y >= height - 3 && height <= this.waterLevel + 2) {
            chunk.set(x, y, z, Blocks.SAND.createMeta());
            return true;
        }

        return false;

    }
}

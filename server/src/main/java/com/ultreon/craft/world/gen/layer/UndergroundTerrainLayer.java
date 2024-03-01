package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.Nullable;

public class UndergroundTerrainLayer extends TerrainLayer {
    private final Block block;
    private final int offset;

    public UndergroundTerrainLayer(Block block, int offset) {
        this.block = block;
        this.offset = offset;
    }

    @Override
    public @Nullable Block handle(World world, Chunk chunk, int x, int y, int z, int height) {
        return y <= height - offset ? this.block : null;
    }
}

package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.Nullable;

public class SurfaceTerrainLayer extends TerrainLayer {
    private final Block surfaceBlock;
    private final int height;

    public SurfaceTerrainLayer(Block surfaceBlock, int height) {
        this.surfaceBlock = surfaceBlock;
        this.height = height;
    }

    @Override
    public @Nullable Block handle(World world, Chunk chunk, int x, int y, int z, int height) {
        return y >= height - this.height && y <= height ? this.surfaceBlock : null;
    }
}

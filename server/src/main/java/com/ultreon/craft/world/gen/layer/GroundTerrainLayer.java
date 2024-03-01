package com.ultreon.craft.world.gen.layer;

import com.google.common.base.Preconditions;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.Nullable;

public class GroundTerrainLayer extends TerrainLayer {
    private final Block block;
    private final int offset;
    private final int height;

    public GroundTerrainLayer(Block block, int offset, int height) {
        Preconditions.checkArgument(height > 0, "Height must be greater than zero");

        this.block = block;
        this.offset = offset;
        this.height = height;
    }

    @Override
    public @Nullable Block handle(World world, Chunk chunk, int x, int y, int z, int height) {
        return y > height - this.offset - this.height && y <= height - this.offset ? this.block : null;
    }
}

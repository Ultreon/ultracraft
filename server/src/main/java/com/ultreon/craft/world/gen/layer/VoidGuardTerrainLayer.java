package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.Nullable;

public class VoidGuardTerrainLayer extends TerrainLayer {
    public VoidGuardTerrainLayer() {

    }

    @Override
    public @Nullable Block handle(World world, Chunk chunk, int x, int y, int z, int height) {
        return null;
    }
}

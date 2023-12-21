package com.ultreon.craft.world.gen.biome;

import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.layer.Decorator;

public class CaveProcessor extends Decorator {
    @Override
    public boolean handle(World world, Chunk chunk, int x, int y, int z, int height) {
        return false;
    }
}

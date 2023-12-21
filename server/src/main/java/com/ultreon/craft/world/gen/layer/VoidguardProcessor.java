package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;

public class VoidguardProcessor extends Decorator {
    @Override
    public boolean handle(World world, Chunk chunk, int x, int y, int z, int height) {
        if (y == 0) {
            chunk.set(x, 0, z, Blocks.VOIDGUARD);
            return true;
        }
        return false;
    }
}

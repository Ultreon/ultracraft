package com.ultreon.craft.world.gen.layer;

import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;

public abstract class TerrainLayer implements Disposable {
    public abstract boolean handle(World world, Chunk chunk, int x, int y, int z, int height, long seed);

    @Override
    public void dispose() {

    }
}

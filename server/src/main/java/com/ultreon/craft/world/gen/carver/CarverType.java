package com.ultreon.craft.world.gen.carver;

import com.ultreon.craft.world.BuilderChunk;
import com.ultreon.craft.world.ServerWorld;

public interface CarverType {
    void carve(ServerWorld world, BuilderChunk chunk, Carver instance, int x, int y, int z);

    Carver create(long seed);
}

package com.ultreon.craft.world.gen;

import com.ultreon.craft.world.ChunkPos;

import java.util.List;

public final class WorldGenInfo {
    public List<ChunkPos> toCreate;
    public List<ChunkPos> toRemove;
    public List<ChunkPos> toUpdate;

    public WorldGenInfo() {

    }
}

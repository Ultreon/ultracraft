package com.ultreon.craft.world.gen;

import com.ultreon.craft.world.ChunkPos;

import java.util.ArrayList;
import java.util.List;

public final class WorldGenInfo {
    public List<ChunkPos> toCreate = new ArrayList<>();
    public List<ChunkPos> toRemove = new ArrayList<>();
    public List<ChunkPos> toUpdate = new ArrayList<>();

    public WorldGenInfo() {

    }
}

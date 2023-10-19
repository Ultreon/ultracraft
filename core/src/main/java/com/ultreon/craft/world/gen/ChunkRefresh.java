package com.ultreon.craft.world.gen;

import com.ultreon.craft.world.ChunkPos;

import java.util.ArrayList;
import java.util.List;

public final class ChunkRefresh {
    public final List<ChunkPos> toCreate;
    public final List<ChunkPos> toRemove;

    public ChunkRefresh() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public ChunkRefresh(List<ChunkPos> toCreate, List<ChunkPos> toRemove) {
        this.toCreate = toCreate;
        this.toRemove = toRemove;
    }
}

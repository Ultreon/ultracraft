package com.ultreon.craft.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChunkRefresher {
    protected final List<ChunkPos> toLoad = new ArrayList<>();
    protected final List<ChunkPos> toUnload = new ArrayList<>();
    private boolean frozen = false;

    public ChunkRefresher() {

    }

    public void addLoading(Collection<ChunkPos> toLoad) {
        if (this.frozen) return;
        for (ChunkPos pos : toLoad) {
            if (this.toLoad.contains(pos)) continue;

            this.toLoad.add(pos);
            this.toUnload.remove(pos);
        }
    }

    public void addUnloading(Collection<ChunkPos> toLoad) {
        if (this.frozen) return;
        for (ChunkPos pos : toLoad) {
            if (this.toLoad.contains(pos)) continue;

            this.toUnload.add(pos);
        }
    }

    public void freeze() {
        this.frozen = true;
    }

    public boolean isFrozen() {
        return this.frozen;
    }
}

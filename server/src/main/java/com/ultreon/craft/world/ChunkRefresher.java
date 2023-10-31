package com.ultreon.craft.world;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChunkRefresher {
    protected final Set<ChunkPos> toLoad = new HashSet<>();
    protected final Set<ChunkPos> toUnload = new HashSet<>();
    private boolean frozen = false;

    public ChunkRefresher() {

    }

    public void addLoading(Collection<ChunkPos> toLoad) {
        if (this.frozen) return;
        this.toLoad.addAll(toLoad);
        toLoad.forEach(this.toUnload::remove);
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

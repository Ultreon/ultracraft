package com.ultreon.craft.world.gen;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.BuilderChunk;
import com.ultreon.craft.world.ChunkAccess;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.libs.commons.v0.vector.Vec3i;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class RecordingChunk implements ChunkAccess {
    private final BuilderChunk chunk;
    private final Set<ServerWorld.RecordedChange> recordedChanges = new HashSet<>();

    public RecordingChunk(BuilderChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public boolean setFast(int x, int y, int z, Block block) {
        this.recordedChanges.add(new ServerWorld.RecordedChange(x, y, z, block));

        return true;
    }

    @Override
    public boolean set(int x, int y, int z, Block block) {
        this.recordedChanges.add(new ServerWorld.RecordedChange(x, y, z, block));

        return true;
    }

    @Override
    public Block getFast(int x, int y, int z) {
        return this.chunk.getFast(x, y, z);
    }

    @Override
    public Block get(int x, int y, int z) {
        return this.chunk.get(x, y, z);
    }

    Collection<ServerWorld.RecordedChange> getRecordedChanges() {
        return this.recordedChanges;
    }

    @Override
    public Vec3i getOffset() {
        return this.chunk.getOffset();
    }

    @Override
    public int getHighest(int x, int z) {
        return this.chunk.getWorld().getHighest(x, z);
    }

    public boolean isOutOfBounds(int x, int y, int z) {
        return this.chunk.isOutOfBounds(x, y, z);
    }

    public ChunkPos getPos() {
        return this.chunk.getPos();
    }
}

package com.ultreon.craft.world;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.util.InvalidThreadException;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;

import static com.ultreon.craft.world.World.CHUNK_SIZE;

public final class BuilderChunk extends Chunk {
    private final ServerWorld world;
    private final Thread thread;

    public BuilderChunk(ServerWorld world, Thread thread, int size, int height, ChunkPos pos) {
        super(world, size, height, pos);
        this.world = world;
        this.thread = thread;
    }

    @Override
    public byte[] serializeChunk() {
        throw new UnsupportedOperationException("Can't serialize builder chunk.");
    }

    @Override
    public void deserializeChunk(byte[] bytes) {
        throw new UnsupportedOperationException("Can't deserialize builder chunk.");
    }

    @Override
    public Block getFast(int x, int y, int z) {
        if (!this.isOnBuilderThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        return super.getFast(x, y, z);
    }

    @Override
    public void setFast(int x, int y, int z, Block block) {
        if (!this.isOnBuilderThread()) throw new InvalidThreadException("Should be on the dedicated builder thread!");
        super.setFast(x, y, z, block);
    }

    @Override
    public ServerWorld getWorld() {
        return this.world;
    }

    public boolean isOnBuilderThread() {
        return this.thread.getId() == Thread.currentThread().getId();
    }

    public ServerChunk build() {
        return new ServerChunk(this.world, this.size, this.height, this.getPos(), this.storage);
    }
}

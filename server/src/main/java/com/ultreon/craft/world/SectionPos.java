package com.ultreon.craft.world;

import com.ultreon.libs.commons.v0.vector.Vec3i;

import static com.ultreon.craft.world.World.CHUNK_SIZE;

public record SectionPos(int x, int y, int z) {
    public SectionPos(Vec3i pos) {
        this(pos.x, pos.y, pos.z);
    }

    public SectionPos(long i) {
        this((int) (i & 0x3fL), (int) ((i >> 6) & 0x3fL), (int) ((i >> 12) & 0x3fL));
    }

    public long compact() {
        return this.x + ((long) this.y << 6) + ((long) this.z << 12);
    }

    public ChunkPos toChunkPos() {
        return new ChunkPos(this.x, this.z);
    }
}

package com.ultreon.craft.world;

import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class ChunkPos {
    private final int x;
    private final int z;

    public ChunkPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int x() {
        return this.x;
    }

    public int z() {
        return this.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ChunkPos chunkPos = (ChunkPos) o;
        return this.x == chunkPos.x && this.z == chunkPos.z;
    }

    @Override
    public String toString() {
        return this.x + "," + this.z;
    }

    @Nullable
    public static RegionPos parse(String s) {
        String[] split = s.split(",", 2);
        Integer x = parseInt(split[0]);
        Integer z = parseInt(split[1]);
        if (x == null) return null;
        if (z == null) return null;
        return new RegionPos(x, z);
    }

    @Nullable
    private static Integer parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Vec3d getChunkOrigin() {
        return new Vec3d(this.x * World.CHUNK_SIZE, World.WORLD_DEPTH, this.z * World.CHUNK_SIZE);
    }
}

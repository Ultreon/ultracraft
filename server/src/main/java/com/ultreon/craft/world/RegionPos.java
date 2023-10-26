package com.ultreon.craft.world;

import com.ultreon.libs.commons.v0.vector.Vec3f;
import com.ultreon.libs.commons.v0.vector.Vec3i;

import java.util.Objects;

public final class RegionPos {
    private final int x;
    private final int z;

    public RegionPos(int x, int z) {
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
        RegionPos chunkPos = (RegionPos) o;
        return this.x == chunkPos.x && this.z == chunkPos.z;
    }

    @Override
    public String toString() {
        return this.x + "," + this.z;
    }

    public Vec3i toVector3() {
        return new Vec3i(this.x * World.CHUNK_SIZE, World.WORLD_DEPTH, this.z * World.CHUNK_SIZE);
    }
}

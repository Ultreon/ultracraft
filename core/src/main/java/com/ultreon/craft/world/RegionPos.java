package com.ultreon.craft.world;

import com.badlogic.gdx.math.Vector3;

public record RegionPos(int x, int z) {
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

    public Vector3 toVector3() {
        return new Vector3(this.x * World.CHUNK_SIZE, World.WORLD_DEPTH, this.z * World.CHUNK_SIZE);
    }
}

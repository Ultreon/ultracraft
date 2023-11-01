package com.ultreon.craft.world;

import com.ultreon.libs.commons.v0.vector.Vec2d;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;

public record ChunkPos(int x, int z) implements Comparable<ChunkPos>, Serializable {
    @Serial
    private static final long serialVersionUID = 782820744815861493L;

    @Override
    public String toString() {
        return this.x + "," + this.z;
    }

    @Nullable
    public static RegionPos parse(String s) {
        String[] split = s.split(",", 2);
        Integer x = ChunkPos.parseInt(split[0]);
        Integer z = ChunkPos.parseInt(split[1]);
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

    public int compareTo(ChunkPos chunkPos) {
        double dst = new Vec2d(this.x, this.z).dst(chunkPos.x, chunkPos.z);
        return dst == 0 ? 0 : dst < 0 ? -1 : 1;
    }
}

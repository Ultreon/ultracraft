package com.ultreon.craft.world;

import com.ultreon.libs.commons.v0.vector.Vec2d;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents the position of a chunk in the world.
 *
 * @param x The x coordinate.
 * @param z The z coordinate.
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public record ChunkPos(int x, int z) implements Comparable<ChunkPos>, Serializable {
    @Serial
    private static final long serialVersionUID = 782820744815861493L;

    /**
     * Converts this chunk position to a string.
     *
     * @return The string representation of this chunk position.
     */
    @Override
    public String toString() {
        return this.x + "," + this.z;
    }

    /**
     * Parse a string into a chunk position.
     *
     * @param s The string to parse.
     * @return The parsed chunk position, or {@code null} if the string cannot be parsed.
     */
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

    /**
     * @return The origin of the chunk.
     */
    public Vec3d getChunkOrigin() {
        return new Vec3d(this.x * World.CHUNK_SIZE, World.WORLD_DEPTH, this.z * World.CHUNK_SIZE);
    }

    /**
     * Compare this chunk position to another.
     *
     * @param chunkPos the chunk positon to be compared.
     * @return the comparison result.
     */
    public int compareTo(ChunkPos chunkPos) {
        double dst = new Vec2d(this.x, this.z).dst(chunkPos.x, chunkPos.z);
        return dst == 0 ? 0 : dst < 0 ? -1 : 1;
    }
}

package com.ultreon.craft.world;

import com.ultreon.libs.commons.v0.vector.Vec2d;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents the position of a chunk in the world.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public final class ChunkPos implements Comparable<ChunkPos>, Serializable {
    @Serial
    private static final long serialVersionUID = 782820744815861493L;
    private final int x;
    private final int y;
    private final int z;

    /**
     * Creates a new chunk position.
     *
     * @param x The x coordinate.
     * @param z The z coordinate.
     * @deprecated Use {@link #ChunkPos(int, int, int)} instead
     */
    @Deprecated
    public ChunkPos(int x, int z) {
        this(x, 0, z);
    }

    /**
     * Creates a new chunk position.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     */
    public ChunkPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Converts this chunk position to a string.
     *
     * @return The string representation of this chunk position.
     */
    @Override
    public String toString() {
        return this.x + "," + this.y + "," + this.z;
    }

    /**
     * Parse a string into a chunk position.
     *
     * @param s The string to parse.
     * @return The parsed chunk position, or {@code null} if the string cannot be parsed.
     */
    @Nullable
    public static ChunkPos parse(String s) {
        String[] split = s.split(",", 2);
        Integer x = ChunkPos.parseInt(split[0]);
        if (x == null) return null;
        if (split.length == 2) {
            Integer z = ChunkPos.parseInt(split[1]);
            return z == null ? null : new ChunkPos(x, 0, z);
        }
        Integer y = ChunkPos.parseInt(split[1]);
        Integer z = ChunkPos.parseInt(split[2]);
        return y == null || z == null ? null : new ChunkPos(x, y, z);

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
        return new Vec3d(this.x * World.CHUNK_SIZE, this.y * World.CHUNK_SIZE, this.z * World.CHUNK_SIZE);
    }

    /**
     * Compare this chunk position to another.
     *
     * @param chunkPos the chunk position to be compared.
     * @return the comparison result.
     */
    @Override
    public int compareTo(ChunkPos chunkPos) {
        double dst = new Vec2d(this.x, this.z).dst(chunkPos.x, chunkPos.z);
        return dst == 0 ? 0 : dst < 0 ? -1 : 1;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }

    public int z() {
        return this.z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ChunkPos) obj;
        return this.x == that.x &&
               this.y == that.y &&
               this.z == that.z;
    }

    @Override
    public int hashCode() {
        int result = this.x;
        result = 31 * result + this.y;
        result = 31 * result + this.z;
        return result;
    }

    public Vec3d vec() {
        return new Vec3d(this.x, this.y, this.z);
    }
}

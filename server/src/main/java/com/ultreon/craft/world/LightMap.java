package com.ultreon.craft.world;

import static com.ultreon.craft.world.World.CHUNK_HEIGHT;
import static com.ultreon.craft.world.World.CHUNK_SIZE;

/**
 * Represents the light map of a chunk.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public class LightMap {
    private byte[] data;

    /**
     * Creates a new light map.
     *
     * @param size the size of the light map
     */
    public LightMap(int size) {
        this.data = new byte[size];
    }

    public LightMap(byte[] data) {
        this.data = data;
    }

    /**
     * Gets the index of the given position.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     * @return  the raw index pointer
     */
    private int getIndex(int x, int y, int z) {
        if (x >= 0 && x < CHUNK_SIZE && y >= 0 && y < CHUNK_HEIGHT && z >= 0 && z < CHUNK_SIZE) {
            return z * (CHUNK_SIZE * CHUNK_HEIGHT) + y * CHUNK_SIZE + x;
        }
        return -1; // Out of bounds
    }

    /**
     * Gets the raw light data for saving.
     *
     * @return the raw light data
     */
    public byte[] getData() {
        return this.data;
    }

    /**
     * Gets the sunlight level at the given position.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     * @return  the sunlight level
     */
    public int getSunlight(int x, int y, int z) {
        byte datum = this.data[this.getIndex(x, y, z)];
        return datum >> 4;
    }

    /**
     * Gets the block light level at the given position.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     * @return  the block light level
     */
    public int getBlockLight(int x, int y, int z) {
        byte datum = this.data[this.getIndex(x, y, z)];
        return datum & 0x0F;
    }

    /**
     * Sets the sunlight level at the given position.
     *
     * @param x     the x-coordinate
     * @param y     the y-coordinate
     * @param z     the z-coordinate
     * @param value the sunlight level
     */
    public void setSunlight(int x, int y, int z, int value) {
        byte datum = this.data[this.getIndex(x, y, z)];
        datum = (byte) ((datum & 0x0F) | (value << 4));
        this.data[this.getIndex(x, y, z)] = datum;
    }

    /**
     * Sets the block light level at the given position.
     *
     * @param x     the x-coordinate
     * @param y     the y-coordinate
     * @param z     the z-coordinate
     * @param value the block light level
     */
    public void setBlockLight(int x, int y, int z, int value) {
        byte datum = this.data[this.getIndex(x, y, z)];
        datum = (byte) ((datum & 0xF0) | value);
        this.data[this.getIndex(x, y, z)] = datum;
    }

    public byte[] save() {
        return data;
    }

    public void load(byte[] data) {
        if (data == null) return;
        this.data = data;
    }
}

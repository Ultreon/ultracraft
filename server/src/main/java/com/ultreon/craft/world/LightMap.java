package com.ultreon.craft.world;

import static com.ultreon.craft.world.World.CHUNK_SIZE;

public class LightMap {
    private byte[] data;

    public LightMap(int size) {
        this.data = new byte[size];
    }

    public LightMap(byte[] data) {
        this.data = data;
    }

    private int getIndex(int x, int y, int z) {
        if (x >= 0 && x < CHUNK_SIZE && y >= 0 && y < CHUNK_SIZE && z >= 0 && z < CHUNK_SIZE) {
            return z * (CHUNK_SIZE * CHUNK_SIZE) + y * CHUNK_SIZE + x;
        }
        return -1; // Out of bounds
    }

    public byte[] getData() {
        return this.data;
    }

    public int getSunlight(int x, int y, int z) {
        byte datum = this.data[this.getIndex(x, y, z)];
        return datum >> 4;
    }

    public int getBlockLight(int x, int y, int z) {
        byte datum = this.data[this.getIndex(x, y, z)];
        return datum & 0x0F;
    }

    public void setSunlight(int x, int y, int z, int value) {
        byte datum = this.data[this.getIndex(x, y, z)];
        datum = (byte) ((datum & 0x0F) | (value << 4));
        this.data[this.getIndex(x, y, z)] = datum;
    }

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

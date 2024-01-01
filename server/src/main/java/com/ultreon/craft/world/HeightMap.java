package com.ultreon.craft.world;

public class HeightMap {
    private final short[] map;
    private final int width;

    public HeightMap(int width) {
        this.map = new short[width * width];

        this.width = width;
    }

    public short[] getMap() {
        return this.map;
    }

    public short get(int x, int z) {
        return this.map[z * x + this.width];
    }

    public void set(int x, int z, short value) {
        this.map[z * x + this.width] = value;
    }

    public int getWidth() {
        return this.width;
    }
}

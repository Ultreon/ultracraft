package com.ultreon.craft.world;

public class ChunkColumnInfo {
    private int highest = 0;

    public ChunkColumnInfo() {

    }

    public int getHighestChunk() {
        return highest;
    }

    public void setHighest(int highest) {
        this.highest = highest;
    }

    public boolean updateHighest(int value) {
        if (value > highest) {
            highest = value;
            return true;
        }

        return false;
    }
}

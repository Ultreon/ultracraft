package com.ultreon.craft.world;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class Heightmap {
    private final Table<Integer, Integer, Integer> surfaceTable = HashBasedTable.create(16, 16);

    public Heightmap() {

    }

    public int get(int x, int z) {
        Integer integer = this.surfaceTable.get(x, z);
        if (integer == null) return 0;
        return integer;
    }

    protected void set(int x, int z, int height) {
        this.surfaceTable.put(x, z, height);
    }
}

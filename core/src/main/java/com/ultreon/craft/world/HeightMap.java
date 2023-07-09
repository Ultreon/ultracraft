package com.ultreon.craft.world;

import com.badlogic.gdx.utils.ShortArray;
import com.ultreon.craft.ubo.DataHolder;
import com.ultreon.data.types.ShortArrayType;

public class HeightMap implements DataHolder<ShortArrayType> {
    private ShortArray data;
    public final int maxSize;
    private int size = 0;

    public HeightMap() {
        int size = 256;
        this.maxSize = size;
        this.data = new ShortArray(size);

        this.data.size = size;
    }

    public ShortArrayType save() {
        return new ShortArrayType(this.data.items);
    }

    @Override
    public void load(ShortArrayType data) {
        this.data.items = data.getValue();
    }

    public void set(int x, int z, int height) {
        this.data.set(this.toIndex(x, z), (short) height);
    }

    public int get(int x, int z) {
        return this.data.get(this.toIndex(x, z));
    }

    private int toIndex(int x, int z) {
        return x + z * this.size;
    }

    public void dispose() {
        this.data = null;
    }
}

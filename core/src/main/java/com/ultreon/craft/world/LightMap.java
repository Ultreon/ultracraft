package com.ultreon.craft.world;

import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.ShortArray;
import com.ultreon.craft.ubo.DataHolder;
import com.ultreon.craft.collection.PaletteSizeException;
import com.ultreon.data.types.MapType;

public class LightMap implements DataHolder<MapType> {
    private ByteArray palette;
    private ShortArray references;
    public final int maxSize;
    private int size = 0;

    public LightMap(int size) {
        if (size > 65536) throw new PaletteSizeException("Size exceeds maximum value of 65536");
        this.maxSize = size;
        this.palette = new ByteArray(size);
        this.references = new ShortArray(size);

        this.palette.size = size;
        this.references.size = size;
    }

    public MapType save() {
        MapType data = new MapType();

        data.putByteArray("LightMap", this.palette.items);
        data.putShortArray("Data", this.references.items);

        return data;
    }

    @Override
    public void load(MapType data) {
        this.palette.items = data.getByteArray("LightMap");
        this.references.items = data.getShortArray("Data");
    }

    public void set(int index, byte value) {
        short old = this.references.get(index);

        int i = this.palette.indexOf(value);
        if (i == -1) {
            i = this.size;
            this.palette.set(i, value);
            this.size++;
        }
        this.references.set(index, (short) i);

        if (!this.references.contains(old)) {
            this.palette.removeIndex(old);
            this.size--;
        }
    }

    public byte get(int index) {
        short paletteIndex = this.references.get(index);
        return this.palette.get(paletteIndex);
    }

    public void dispose() {
        this.palette = null;
        this.references = null;
    }
}

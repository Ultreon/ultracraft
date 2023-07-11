package com.ultreon.craft.collection;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;
import com.ultreon.craft.ubo.DataHolder;
import com.ultreon.craft.ubo.DataWriter;
import com.ultreon.craft.util.exceptions.PaletteSizeException;
import com.ultreon.data.types.*;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class PaletteContainer<T extends IType<?>, D extends DataWriter<T>> implements DataHolder<MapType> {
    private Array<@Nullable D> palette;
    private ShortArray references;
    private final Function<T, D> deserializer;
    private final int dataId;
    public final int maxSize;
    private int size = 0;

    /**
     * @param type DON'T USE: Reserved for data receiver.
     */
    @SafeVarargs
    public PaletteContainer(int size, int dataId, Function<T, D> deserializer, D... type) {
        if (size > 65536) throw new PaletteSizeException("Size exceeds maximum value of 65536");
        this.maxSize = size;
        this.palette = new Array<>(true, size, type.getClass().getComponentType());
        this.references = new ShortArray(size);
        this.dataId = dataId;
        this.deserializer = deserializer;

        this.palette.size = size;
        this.references.size = size;
    }

    public MapType save() {
        MapType data = new MapType();

        ListType<T> paletteData = new ListType<>(this.dataId);
        for (@Nullable D t : this.palette.items) if (t != null) paletteData.add(t.save());
        data.put("Palette", paletteData);

        data.putShortArray("Data", this.references.items);

        return data;
    }

    @Override
    public void load(MapType data) {
        ListType<T> paletteData = data.getList("Palette", new ListType<>(this.dataId));
        int i = 0;
        for (T t : paletteData) {
            this.palette.set(i, this.deserializer.apply(t));
            i++;
        }

        this.references.items = data.getShortArray("Data");
    }

    public void set(int index, D value) {
        short old = this.references.get(index);

        int i = this.palette.indexOf(value, true);
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

    @Nullable
    public D get(int index) {
        short paletteIndex = this.references.get(index);
        return this.palette.get(paletteIndex);
    }

    @SuppressWarnings("DataFlowIssue")
    public void dispose() {
        this.palette = null;
        this.references = null;
    }
}

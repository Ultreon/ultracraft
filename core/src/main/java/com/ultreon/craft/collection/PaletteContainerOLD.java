package com.ultreon.craft.collection;

import com.badlogic.gdx.utils.ShortArray;
import com.ultreon.craft.ubo.DataHolder;
import com.ultreon.craft.ubo.DataWriter;
import com.ultreon.craft.util.exceptions.PaletteSizeException;
import com.ultreon.data.types.IType;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class PaletteContainerOLD<T extends IType<?>, D extends DataWriter<T>> implements DataHolder<MapType> {
    private final List<@Nullable D> data;
    private ShortArray palette;
    private final Function<T, D> deserializer;
    private final int dataId;
    public final int maxSize;
    private final Object lock = new Object();

    /**
     * @param ignoredType DON'T USE: Reserved for data receiver.
     */
    @SafeVarargs
    public PaletteContainerOLD(int size, int dataId, Function<T, D> deserializer, D... ignoredType) {
        if (size > 65536) throw new PaletteSizeException("Size exceeds maximum value of 65536");
        this.maxSize = size;
        this.data = new CopyOnWriteArrayList<>();
        this.palette = new ShortArray(size);
        this.dataId = dataId;
        this.deserializer = deserializer;

        this.palette.size = size;
    }

    public MapType save() {
        MapType data = new MapType();

        ListType<T> paletteData = new ListType<>(this.dataId);
        for (@Nullable D t : this.data) if (t != null) paletteData.add(t.save());
        data.put("Palette", paletteData);

        data.putShortArray("Data", this.palette.items);

        return data;
    }

    @Override
    public void load(MapType data) {
        ListType<T> paletteData = data.getList("Palette", new ListType<>(this.dataId));
        for (T t : paletteData) {
            this.data.add(this.deserializer.apply(t));
        }

        this.palette.items = data.getShortArray("Data");
    }

    public void set(int index, D value) {
        synchronized (this.lock) {
            short old = this.palette.get(index);

            int i = this.data.indexOf(value);
            if (i == -1) {
                i = this.data.size();
                this.data.add(value);
            }
            this.palette.set(index, (short) i);

            if (!this.palette.contains(old)) {
                this.data.remove(old);
            }
        }
    }

    @Nullable
    public D get(int index) {
        short paletteIndex = this.palette.get(index);
        if (this.data.isEmpty()) return null;
        return this.data.get(paletteIndex);
    }

    public void dispose() {
        this.data.clear();
        this.palette = null;
    }
}

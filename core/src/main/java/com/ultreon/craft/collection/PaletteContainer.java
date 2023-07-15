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

public class PaletteContainer<T extends IType<?>, D extends DataWriter<T>> implements DataHolder<MapType> {
    private List<@Nullable D> palette;
    private ShortArray references;
    private final Function<T, D> deserializer;
    private final int dataId;
    public final int maxSize;
    private int size = 0;
    private final Object lock = new Object();

    /**
     * @param type DON'T USE: Reserved for data receiver.
     */
    @SafeVarargs
    public PaletteContainer(int size, int dataId, Function<T, D> deserializer, D... type) {
        if (size > 65536) throw new PaletteSizeException("Size exceeds maximum value of 65536");
        this.maxSize = size;
        this.palette = new CopyOnWriteArrayList<>();
        this.references = new ShortArray(size);
        this.dataId = dataId;
        this.deserializer = deserializer;

        this.references.size = size;
    }

    public MapType save() {
        MapType data = new MapType();

        ListType<T> paletteData = new ListType<>(this.dataId);
        for (@Nullable D t : this.palette) if (t != null) paletteData.add(t.save());
        data.put("Palette", paletteData);

        data.putShortArray("Data", this.references.items);

        return data;
    }

    @Override
    public void load(MapType data) {
        ListType<T> paletteData = data.getList("Palette", new ListType<>(this.dataId));
        for (T t : paletteData) {
            this.palette.add(this.deserializer.apply(t));
        }

        this.references.items = data.getShortArray("Data");
    }

    public void set(int index, D value) {
        synchronized (this.lock) {
            short old = this.references.get(index);

            int i = this.palette.indexOf(value);
            if (i == -1) {
                i = this.palette.size();
                this.palette.add(value);
            }
            this.references.set(index, (short) i);

            if (!this.references.contains(old)) {
                this.palette.remove(old);
            }
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

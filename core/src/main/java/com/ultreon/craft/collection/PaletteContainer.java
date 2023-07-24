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
    private List<@Nullable D> values;
    private ShortArray mapping;
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
        this.values = new CopyOnWriteArrayList<>();
        this.mapping = new ShortArray(size);
        this.dataId = dataId;
        this.deserializer = deserializer;

        this.mapping.size = size;
    }

    public MapType save() {
        MapType data = new MapType();

        ListType<T> paletteData = new ListType<>(this.dataId);
        for (@Nullable D t : this.values) if (t != null) paletteData.add(t.save());
        data.put("Palette", paletteData);

        data.putShortArray("Data", this.mapping.items);

        return data;
    }

    @Override
    public void load(MapType data) {
        ListType<T> paletteData = data.getList("Palette", new ListType<>(this.dataId));
        for (T t : paletteData) {
            this.values.add(this.deserializer.apply(t));
        }

        this.mapping.items = data.getShortArray("Data");
    }

    public void set(int index, D value) {
        synchronized (this.lock) {
            short oPaletteIndex = this.mapping.get(index);

            int i = this.values.indexOf(value);
            if (i == -1) {
                i = this.values.size();
                this.values.add(value);
            }
            this.mapping.set(index, (short) i);

            if (!this.mapping.contains(oPaletteIndex)) {
                this.values.remove(oPaletteIndex);
            }
        }
    }

    @Nullable
    public D get(int index) {
        synchronized (this.lock) {
            short paletteIndex = this.mapping.get(index);
            return this.values.get(paletteIndex);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public void dispose() {
        this.values = null;
        this.mapping = null;
    }
}

package com.ultreon.craft.collection;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ShortArray;
import com.ultreon.craft.ubo.DataHolder;
import com.ultreon.craft.ubo.DataWriter;
import com.ultreon.data.types.IType;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PaletteContainer<T extends IType<?>, D extends DataWriter<T>> implements DataHolder<MapType>, Disposable {
    private final ShortArray palette;
    private final List<D> data;
    private final int dataId;
    private final Function<T, D> deserializer;

    @SafeVarargs
    public PaletteContainer(int size, int dataId, Function<T, D> deserializer, D... ignoredType) {
        this.palette = new ShortArray();
        this.data = new ArrayList<>();
        this.dataId = dataId;
        this.deserializer = deserializer;
        this.palette.setSize(size);
    }

    public MapType save() {
        MapType data = new MapType();

        ListType<T> paletteData = new ListType<>(this.dataId);
        for (@Nullable D t : this.data) if (t != null) paletteData.add(t.save());
        data.put("Data", paletteData);

        data.putShortArray("Palette", this.palette.items);

        return data;
    }

    @Override
    public void load(MapType data) {
        ListType<T> paletteData = data.getList("Data", new ListType<>(this.dataId));
        for (T t : paletteData) {
            this.data.add(this.deserializer.apply(t));
        }

        this.palette.items = data.getShortArray("Palette");
    }

    public void set(int idx, D value) {
        if (value == null) {
            this.remove(idx);
            return;
        }


        short old = this.palette.get(idx);

        short setIdx = (short) this.data.indexOf(value);
        if (setIdx == -1) {
            setIdx = this.add(value);
        }
        this.palette.set(idx, setIdx);

        if (!this.palette.contains(old)) {
            System.out.println("old = " + old);
            this.data.remove(old);

            // Update paletteMap entries for indices after the removed one
            for (int i = idx; i < this.palette.size; i++) {
                int oldValue = this.palette.get(i);
                this.palette.set(i, (short) (oldValue - 1));
            }
        }

//        if (!this.data.contains(value)) {
//            this.add(value);
//        } else {
//            int dataIdx = this.toDataIdx(idx);
//            if (dataIdx != -1) {
//                this.data.set(dataIdx, value);
//            } else {
//
//            }
//        }
    }

    public short toDataIdx(int idx) {
        if (idx >= 0 && idx < this.palette.size) {
            return this.palette.get(idx);
        }
        return -1;
    }

    public D getFromDataIdx(int dataIdx) {
        if (dataIdx >= 0 && dataIdx < this.data.size()) {
            return this.data.get(dataIdx);
        }
        return null; // Or throw an exception if you prefer
    }

    public short add(D value) {
        short dataIdx = (short) (this.data.size());
        this.data.add(value);
        this.palette.add((short) this.palette.size, dataIdx);
        return dataIdx;
    }

    public void remove(int idx) {
        if (idx >= 0 && idx < this.data.size()) {
            int dataIdx = this.toDataIdx(idx);
            this.data.remove(dataIdx);
            this.palette.removeIndex(idx);

            // Update paletteMap entries for indices after the removed one
            for (int i = idx; i < this.palette.size; i++) {
                int oldValue = this.palette.get(i);
                this.palette.set(i, (short) (oldValue - 1));
            }
        }
    }

    @Override
    public void dispose() {
        this.palette.items = null;
        this.palette.clear();
        this.data.clear();
    }

    @Nullable
    public D get(int idx) {
        int paletteIdx = this.toDataIdx(idx);
        if (paletteIdx < 0) return null;
        return this.getFromDataIdx(paletteIdx);
    }
}

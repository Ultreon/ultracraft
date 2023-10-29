package com.ultreon.craft.collection;

import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.craft.ubo.DataKeys;
import com.ultreon.craft.ubo.DataWriter;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class PaletteStorage<D extends DataWriter<MapType>> implements ServerDisposable {
    private short[] palette;
    private final List<D> data = new ArrayList<>();
    private int paletteCounter = 0;

    public PaletteStorage(int size) {
        this.palette = new short[size];
        Arrays.fill(this.palette, (short) -1);
    }

    public PaletteStorage(MapType data, Function<MapType, D> decoder) {
        ListType<MapType> paletteData = data.getList("Data", new ListType<>());
        for (MapType mapData : paletteData) {
            this.data.add(decoder.apply(mapData));
        }

        this.palette = data.getShortArray("Palette");
    }

    public MapType save(MapType outputData) {
        ListType<MapType> data = new ListType<>();
        for (@Nullable D entry : this.data) if (entry != null) data.add(entry.save());
        outputData.put("Data", data);

        outputData.putShortArray("Palette", this.palette);

        return outputData;
    }

    public void load(MapType inputData, Function<MapType, D> decoder) {
        this.data.clear();
        var data = inputData.<MapType>getList(DataKeys.PALETTE_DATA);
        for (MapType entryData : data) {
            D entry = decoder.apply(entryData);
            this.data.add(entry);
        }

        this.palette = inputData.getShortArray(DataKeys.PALETTE, new short[this.palette.length]);
    }

    public void set(int idx, D value) {
        if (value == null) {
            this.remove(idx);
            return;
        }


        short old = this.palette[idx];

        short setIdx = (short) this.data.indexOf(value);
        if (setIdx == -1) {
            setIdx = this.add(value);
        }
        this.palette[idx] = setIdx;

        if (old >= 0 && !ArrayUtils.contains(this.palette, old)) {
            this.data.remove(old);

            // Update paletteMap entries for indices after the removed one
            for (int i = idx; i < this.palette.length; i++) {
                int oldValue = this.palette[i];
                this.palette[i] = (short) (oldValue - 1);
            }
        }
    }

    public short toDataIdx(int idx) {
        if (idx >= 0 && idx < this.palette.length) {
            return this.palette[idx];
        }
        return -1;
    }

    public D direct(int dataIdx) {
        if (dataIdx >= 0 && dataIdx < this.data.size()) {
            return this.data.get(dataIdx);
        }
        return null; // Or throw an exception if you prefer
    }

    public short add(D value) {
        short dataIdx = (short) (this.data.size());
        this.data.add(value);
        this.palette[this.paletteCounter] = dataIdx;
        return dataIdx;
    }

    public void remove(int idx) {
        if (idx >= 0 && idx < this.data.size()) {
            int dataIdx = this.toDataIdx(idx);
            this.data.remove(dataIdx);
            this.palette[idx] = -1;
            this.paletteCounter--;

            // Update paletteMap entries for indices after the removed one
            for (int i = idx; i < this.palette.length; i++) {
                int oldValue = this.palette[i];
                this.palette[i] = (short) (oldValue - 1);
            }
        }
    }

    @Override
    public void dispose() {
        this.palette = null;
        this.data.clear();
    }

    @Nullable
    public D get(int idx) {
        int paletteIdx = this.toDataIdx(idx);
        if (paletteIdx < 0) return null;
        return this.direct(paletteIdx);
    }
}

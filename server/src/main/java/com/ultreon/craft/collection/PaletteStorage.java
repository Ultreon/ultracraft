package com.ultreon.craft.collection;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.craft.ubo.DataKeys;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * <h1>Palette Storage Implementation.</h1>
 * Palette storage is used for storing data in palettes.
 * It's used for optimizing memory and storage usage.
 * Generally used for advanced voxel games.
 * <p>
 * It makes use of short arrays to store {@link #getPalette() index pointers} to the {@linkplain #getData() data}.
 * While the data itself is stored without any duplicates.
 *
 * @param <D> the data type.
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public class PaletteStorage<D> implements ServerDisposable {
    private short[] palette;
    private List<D> data = new ArrayList<>();
    private int paletteCounter = 0;

    public PaletteStorage(int size) {
        this.palette = new short[size];
        Arrays.fill(this.palette, (short) -1);
    }

    public PaletteStorage(short[] palette, List<D> data) {
        this.palette = palette;
        this.data = data;
    }

    public MapType save(MapType outputData, Function<D, MapType> encoder) {
        ListType<MapType> data = new ListType<>();
        for (@Nullable D entry : this.data) if (entry != null) data.add(encoder.apply(entry));
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

    public void write(PacketBuffer buffer, Function<D, MapType> encoder) {
        buffer.writeInt(this.data.size());
        for (D entry : this.data) {
            buffer.writeUbo(encoder.apply(entry));
        }

        buffer.writeInt(this.palette.length);
        for (short v : this.palette) {
            buffer.writeShort(v);
        }
    }

    public void read(PacketBuffer buffer, Function<MapType, D> decoder) {
        this.data.clear();

        int dataSize = buffer.readInt();
        for (int i = 0; i < dataSize; i++) {
            var ubo = buffer.<MapType>readUbo();
            decoder.apply(ubo);
        }

        short[] palette = new short[buffer.readUnsignedShort()];
        for (int i = 0; i < palette.length; i++) {
            palette[i] = buffer.readShort();
        }
        this.palette = palette;
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

    public short[] getPalette() {
        return this.palette;
    }

    public List<D> getData() {
        return Collections.unmodifiableList(this.data);
    }

    public void set(short[] palette, List<D> data) {
        if (this.palette.length != palette.length) throw new IllegalArgumentException("Palette length must be equal.");

        this.palette = palette;
        this.data = data;
    }
}

package com.ultreon.craft.collection;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.craft.ubo.DataKeys;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>Palette storage is used for storing data in palettes.
 * It's used for optimizing memory and storage usage.
 * Generally used for advanced voxel games.</p>
 *
 * <p>It makes use of short arrays to store {@link #getPalette() index pointers} to the {@linkplain #getData() data}.
 * While the data itself is stored without any duplicates.</p>
 *
 * @param <D> the data type.
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
@NotThreadSafe
public class PaletteStorage<D> implements ServerDisposable, Storage<D> {
    private short[] palette;
    private List<D> data = new LinkedList<>();
    private int paletteCounter = 0;

    public PaletteStorage(int size) {
        this.palette = new short[size];
        Arrays.fill(this.palette, (short) -1);
    }

    public PaletteStorage(short[] palette, List<D> data) {
        this.palette = palette;
        this.data = data;
    }

    @Override
    public MapType save(MapType outputData, Function<D, MapType> encoder) {
        ListType<MapType> data = new ListType<>();
        for (@Nullable D entry : this.data) if (entry != null) data.add(encoder.apply(entry));
        outputData.put("Data", data);

        outputData.putShortArray("Palette", this.palette);

        return outputData;
    }

    @Override
    public void load(MapType inputData, Function<MapType, D> decoder) {
        this.data.clear();
        var data = inputData.<MapType>getList(DataKeys.PALETTE_DATA);
        for (MapType entryData : data.getValue()) {
            D entry = decoder.apply(entryData);
            this.data.add(entry);
        }

        this.palette = inputData.getShortArray(DataKeys.PALETTE, new short[this.palette.length]);
    }

    @Override
    public void write(PacketBuffer buffer, BiConsumer<PacketBuffer, D> encoder) {
        buffer.writeInt(this.data.size());
        for (D entry : this.data) if (entry != null) encoder.accept(buffer, entry);
        buffer.writeInt(this.palette.length);
        for (short v : this.palette) buffer.writeShort(v);
    }

    @Override
    public void read(PacketBuffer buffer, Function<PacketBuffer, D> decoder) {
        var data = new ArrayList<D>();
        var dataSize = buffer.readInt();
        for (int i = 0; i < dataSize; i++) {
            data.add(decoder.apply(buffer));
        }
        this.data = data;

        short[] palette = new short[buffer.readInt()];
        for (int i = 0; i < palette.length; i++) {
            palette[i] = buffer.readShort();
        }
    }

    @Override
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
        return idx >= 0 && idx < this.palette.length ? this.palette[idx] : -1;
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
    @Override
    public D get(int idx) {
        short paletteIdx = this.toDataIdx(idx);
        return paletteIdx < 0 ? null : this.direct(paletteIdx);
    }

    @Override
    public <R> Storage<R> map(Function<D, R> o, Class<R> clazz) {
        var data = this.data.stream().map(o).collect(Collectors.toList());
        return new PaletteStorage<>(this.palette, data);
    }

    public short[] getPalette() {
        return this.palette;
    }

    public List<D> getData() {
        return Collections.unmodifiableList(this.data);
    }

    public void set(short[] palette, List<D> data) {
        if (this.palette.length != palette.length)
            throw new IllegalArgumentException("Palette length must be equal.");

        this.palette = palette;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        PaletteStorage<?> that = (PaletteStorage<?>) o;
        return Arrays.equals(this.palette, that.palette) && this.data.equals(that.data);
    }

    @Override
    public int hashCode() {
        int result = this.data.hashCode();
        result = 31 * result + Arrays.hashCode(this.palette);
        return result;
    }
}

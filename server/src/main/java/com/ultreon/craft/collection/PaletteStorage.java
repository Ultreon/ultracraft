package com.ultreon.craft.collection;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * <p>Palette storage is used for storing data in palettes.
 * It's used for optimizing memory and storage usage.
 * Generally used for advanced voxel games.</p>
 *
 * @param <T> the data type.
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
@NotThreadSafe
public class PaletteStorage<T> implements ServerDisposable {
    public final int length;
    private Palette<T> palette;
    private PaletteData data;

    public PaletteStorage(int size, T value, T... typeGetter) {
        this.palette = new Palette<>(value, typeGetter);
        if (size <= 256) {
            this.data = new BytePaletteData(size);
        } else if (size <= 65536) {
            this.data = new ShortPaletteData(size);
        } else {
            throw new IllegalArgumentException("Palette size too big: " + size);
        }

        this.length = size;
    }

    public PaletteStorage(long[] data, List<T> palette) {
        if (palette.isEmpty()) throw new IllegalArgumentException("Palette cannot be empty.");

        this.palette = new Palette<>(palette, palette.get(0));
        if (data.length <= 256) {
            this.data = new BytePaletteData(data);
        } else if (data.length <= 65536) {
            this.data = new ShortPaletteData(data);
        } else {
            throw new IllegalArgumentException("Palette size too big: " + data.length);
        }

        this.length = data.length;
    }

    public PaletteStorage(long[] data, T[] palette) {
        if (palette.length == 0) throw new IllegalArgumentException("Palette cannot be empty.");

        this.palette = new Palette<>(palette);
        if (data.length <= 256) {
            this.data = new BytePaletteData(data);
        } else if (data.length <= 65536) {
            this.data = new ShortPaletteData(data);
        } else {
            throw new IllegalArgumentException("Palette size too big: " + data.length);
        }

        this.length = data.length;
    }

    @Override
    public void dispose() {
        this.palette.dispose();
    }

    /**
     * Retrieves an element from the palette at the specified index.
     *
     * @param  idx  the index of the element to retrieve
     * @return      the element at the specified index
     */
    public T get(int idx) {
        return this.palette.get((short) this.data.get(idx));
    }

    /**
     * Sets the element at the specified index in the list to the given block.
     *
     * @param  index the index at which to set the block
     * @param  block the new value to be set at the specified index
     */
    public void set(int index, T block) {
        int paletteIdx = this.palette.indexOf(block);
        if (paletteIdx == -1) {
            paletteIdx = this.palette.add(block);

            int oldPaletteIdx = this.data.get(index);
            this.data.set(index, paletteIdx);
            if (!this.data.contains(oldPaletteIdx)) {
                this.palette.remove(oldPaletteIdx);
                this.data.reduce(oldPaletteIdx);
            }
            return;
        }

        this.data.set(index, paletteIdx);
    }

    public long[] getData() {
        return this.data.getData();
    }

    public T[] getPalette() {
        return this.palette.getData();
    }

    public void save(MapType data, Function<T, MapType> encoder) {
        ListType<MapType> palette = new ListType<>();
        for (T entry : this.palette.getData()) palette.add(encoder.apply(entry));
        data.put("Palette", palette);
        data.putLongArray("Data", this.data.getData());
    }

    public void load(MapType data, Function<MapType, T> decoder) {
        ListType<MapType> palette = data.getList("Palette");
        for (MapType entryData : palette.getValue()) {
            T entry = decoder.apply(entryData);
            this.palette.add(entry);
        }

        this.data = new BytePaletteData(data.getLongArray("Data"));
    }

    public void write(PacketBuffer buffer, BiConsumer<T, PacketBuffer> encoder) {
        buffer.writeLongArray(this.data.getData());
        buffer.writeArray(this.palette.getData(), (packetBuffer, t) -> encoder.accept(t, packetBuffer));
    }

    public void read(PacketBuffer buffer, Function<PacketBuffer, T> decoder) {
        for (int i = 0; i < this.length; i++) {
            this.set(i, decoder.apply(buffer));
        }
    }

    public <R> PaletteStorage<R> map(Function<T, R> mapper) {
        List<R> palette = new ArrayList<>();
        for (T entry : this.palette.getData()) palette.add(mapper.apply(entry));

        return new PaletteStorage<>(this.data.getData(), palette);
    }

    /**
     * Sets the data and palette for the palette storage.
     *
     * @param  data    the array of long values representing the data
     * @param  palette the array of T values representing the palette
     * @throws IllegalArgumentException if the current data length is not equal to the given data length, or if the palette size is too big.
     */
    public void set(long[] data, T[] palette) {
        if (this.data.rawSize() != data.length)
            throw new IllegalArgumentException("Data size must be equal, got %d but should be %d".formatted(data.length, this.data.size()));

        this.palette = new Palette<>(palette);

        if (this.data.size() <= 256) {
            this.data = new BytePaletteData(data);
        } else if (this.data.size() <= 65536) {
            this.data = new ShortPaletteData(data);
        } else {
            throw new IllegalArgumentException("Palette size too big: " + data.length);
        }
    }

    public void fill(T value) {
        this.data.fill();
        this.palette.clear(value);
    }
}

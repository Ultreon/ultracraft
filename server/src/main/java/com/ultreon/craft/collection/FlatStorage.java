package com.ultreon.craft.collection;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.ubo.DataKeys;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import it.unimi.dsi.fastutil.objects.Reference2ShortFunction;
import it.unimi.dsi.fastutil.shorts.Short2ReferenceFunction;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FlatStorage<D> implements Storage<D> {
    private D[] data;

    public FlatStorage(D[] data) {
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    public FlatStorage(int size, D... typeGetter) {
        this.data = (D[]) Array.newInstance(typeGetter.getClass().getComponentType(), size);
    }

    @SuppressWarnings("unchecked")
    public FlatStorage(short[] shorts, Short2ReferenceFunction<D> decoder, D... typeGetter) {
        this.data = (D[]) Array.newInstance(typeGetter.getClass().getComponentType(), shorts.length);
        for (int i = 0; i < shorts.length; i++) {
            this.data[i] = decoder.get(shorts[i]);
        }
    }

    @SuppressWarnings("unchecked")
    public FlatStorage(List<D> data, Class<D> type) {
        this.data = (D[]) Array.newInstance(type, data.size());
        for (int i = 0, dataSize = data.size(); i < dataSize; i++) {
            this.data[i] = data.get(i);
        }
    }

    @SuppressWarnings("unchecked")
    public FlatStorage(PacketBuffer buffer, Function<PacketBuffer, D> decoder, D... typeGetter) {
        int size = buffer.readInt();
        this.data = (D[]) Array.newInstance(typeGetter.getClass().getComponentType(), size);

        for (int i = 0; i < size; i++) {
            this.data[i] = decoder.apply(buffer);
        }
    }

    @Override
    public MapType save(MapType outputData, Function<D, MapType> encoder) {
        ListType<MapType> data = new ListType<>();
        for (@Nullable D entry : this.data) if (entry != null) data.add(encoder.apply(entry));

        outputData.put("Data", data);
        return outputData;
    }

    @Override
    public void load(MapType inputData, Function<MapType, D> decoder) {
        var data = inputData.<MapType>getList(DataKeys.PALETTE_DATA);
        List<MapType> value = data.getValue();
        for (int i = 0, valueSize = value.size(); i < valueSize; i++) {
            MapType entryData = value.get(i);
            D entry = decoder.apply(entryData);
            this.data[i] = entry;
        }
    }

    @Override
    public void write(PacketBuffer buffer, BiConsumer<PacketBuffer, D> encoder) {
        buffer.writeInt(this.data.length);
        D[] ds = this.data;
        for (D entry : ds) {
            encoder.accept(buffer, entry);
        }
    }

    @Override
    public void read(PacketBuffer buffer, Function<PacketBuffer, D> decoder) {
        var size = buffer.readInt();
        this.data = Arrays.copyOf(this.data, size);
        for (int i = 0; i < size; i++) {
            D entry = decoder.apply(buffer);
            this.data[i] = entry;
        }
    }

    @Override
    public void set(int idx, D value) {
        if (idx < 0 || idx >= this.data.length) {
            throw new ArrayIndexOutOfBoundsException(idx);
        }

        this.data[idx] = value;
    }

    @Override
    public D get(int idx) {
        if (idx < 0 || idx >= this.data.length) {
            throw new ArrayIndexOutOfBoundsException(idx);
        }

        return this.data[idx];
    }

    public short[] serialize(Reference2ShortFunction<D> encoder) {
        short[] data = new short[this.data.length];
        D[] ds = this.data;
        for (int i = 0, dsLength = ds.length; i < dsLength; i++) {
            var datum = ds[i];
            if (datum != null) {
                data[i] =encoder.getShort(datum);
            }
        }
        return data;
    }

    @Override
    public <R> Storage<R> map(Function<D, R> o, Class<R> clazz) {
        var data = Arrays.stream(this.data).map(o).toList();
        return new FlatStorage<>(data, clazz);
    }
}

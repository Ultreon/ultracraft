package com.ultreon.craft.collection;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.data.types.MapType;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface Storage<D> {

    MapType save(MapType outputData, Function<D, MapType> encoder);

    void load(MapType inputData, Function<MapType, D> decoder);


    void write(PacketBuffer buffer, BiConsumer<PacketBuffer, D> encoder);

    void read(PacketBuffer buffer, Function<PacketBuffer, D> decoder);

    boolean set(int idx, D value);

    D get(int idx);


    <R> Storage<R> map(R defaultValue, Function<D, R> o);
}

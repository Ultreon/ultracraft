package com.ultreon.craft.registry;

public interface RawIdMap<T> {
    T byId(int id);

    int getRawId(T object);
}

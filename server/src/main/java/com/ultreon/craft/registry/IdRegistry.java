package com.ultreon.craft.registry;

public interface IdRegistry<T> {
    T get(int id);

    int getRawId(T object);
}

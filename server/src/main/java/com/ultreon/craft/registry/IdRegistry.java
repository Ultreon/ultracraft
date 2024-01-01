package com.ultreon.craft.registry;

public interface IdRegistry<T> {
    T byId(int id);

    int getId(T object);
}

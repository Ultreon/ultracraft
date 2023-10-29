package com.ultreon.craft.ubo;

@FunctionalInterface
public interface DataWriter<T> {
    T save();
}

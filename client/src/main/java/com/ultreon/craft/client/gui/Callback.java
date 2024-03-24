package com.ultreon.craft.client.gui;

@FunctionalInterface
public interface Callback<T> {
    void call(T caller);
}

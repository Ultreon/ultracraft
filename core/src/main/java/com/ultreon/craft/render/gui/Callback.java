package com.ultreon.craft.render.gui;

@FunctionalInterface
public interface Callback<T> {
    void call(T caller);
}

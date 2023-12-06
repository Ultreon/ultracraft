package com.ultreon.craft.api.commands;

public abstract class Argument<T> {
    private final T value;

    public T get() {
        return this.value;
    }

    public Argument(T value) {
        this.value = value;
    }
}

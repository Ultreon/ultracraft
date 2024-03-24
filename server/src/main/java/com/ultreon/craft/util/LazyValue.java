package com.ultreon.craft.util;

public class LazyValue<T> {
    private T value;
    private boolean initialized;
    private final Object lock = new Object();

    public T get() {
        synchronized (lock) {
            if (!initialized) {
                throw new IllegalStateException("Value not initialized");
            }
            return value;
        }
    }

    public void set(T value) {
        synchronized (lock) {
            if (initialized) {
                throw new IllegalStateException("Value already initialized");
            }

            this.value = value;
            this.initialized = true;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }
}

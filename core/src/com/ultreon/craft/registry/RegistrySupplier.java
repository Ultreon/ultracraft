package com.ultreon.craft.registry;

import com.ultreon.craft.resources.Identifier;

import java.util.function.Supplier;

public class RegistrySupplier<T> implements Supplier<T> {
    private final Registry<? super T> registry;
    private final Identifier id;

    public RegistrySupplier(Registry<? super T> registry, Identifier id) {
        this.registry = registry;
        this.id = id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        T t = (T) registry.get(id);
        if (t == null) {
            throw new IllegalArgumentException("Registry entry not present: " + id);
        }
        return t;
    }
}

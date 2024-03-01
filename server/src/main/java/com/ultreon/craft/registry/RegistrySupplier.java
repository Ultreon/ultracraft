package com.ultreon.craft.registry;

import com.ultreon.craft.util.Identifier;

import java.util.function.Supplier;

@SuppressWarnings({"unchecked"})
public class RegistrySupplier<T> implements Supplier<T> {
    private final Registry<? super T> registry;
    private final Supplier<T> supplier;
    private final Identifier identifier;

    public RegistrySupplier(Registry<? super T> registry, Supplier<T> supplier, Identifier identifier) {
        this.registry = registry;
        this.supplier = supplier;
        this.identifier = identifier;
    }

    public void register() {
        this.registry.register(this.identifier, this.supplier.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) this.registry.get(this.identifier);
    }

    public Identifier id() {
        return this.identifier;
    }
}

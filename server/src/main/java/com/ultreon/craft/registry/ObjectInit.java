package com.ultreon.craft.registry;

import java.util.function.Supplier;

public abstract class ObjectInit<T> {
    protected final DelayedRegister<T> register;

    public ObjectInit(String namespace, Registry<T> registry) {
        this.register = new DelayedRegister<>(namespace, registry);
    }

    public <C extends T> RegistrySupplier<C> register(String name, Supplier<C> supplier) {
        return this.register.register(name, supplier);
    }
}

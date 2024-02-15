package com.ultreon.craft.registry;

import com.ultreon.craft.util.ElementID;

import java.util.function.Supplier;

@SuppressWarnings({"unchecked"})
public class DeferredElement<T> implements Supplier<T> {
    private final Registry<? super T> registry;
    private final Supplier<T> supplier;
    private final ElementID elementID;

    public DeferredElement(Registry<? super T> registry, Supplier<T> supplier, ElementID elementID) {
        this.registry = registry;
        this.supplier = supplier;
        this.elementID = elementID;
    }

    public void register() {
        this.registry.register(this.elementID, this.supplier.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) this.registry.getElement(this.elementID);
    }

    public ElementID id() {
        return this.elementID;
    }
}

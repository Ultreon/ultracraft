package com.ultreon.craft.registry;

import com.ultreon.craft.event.RegistryEvents;
import com.ultreon.craft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Deprecated
public class DelayedRegister<T> {
    private final String namespace;
    private final Registry<T> registry;
    private final Map<Identifier, Supplier<? extends T>> deffer = new HashMap<>();

    public DelayedRegister(String namespace, Registry<T> registry) {
        this.namespace = namespace;
        this.registry = registry;
    }

    public <C extends T> RegistrySupplier<C> register(String name, Supplier<C> supplier) {
        Identifier id = new Identifier(namespace, name);
        deffer.put(id, supplier);
        return new RegistrySupplier<>(registry, id);
    }

    public void register() {
        RegistryEvents.get(registry).listen(reg -> {
            for (var entry : deffer.entrySet())
                reg.set(entry.getKey(), entry.getValue().get());
        });
    }
}

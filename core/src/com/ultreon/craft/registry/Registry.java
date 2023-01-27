package com.ultreon.craft.registry;

import com.ultreon.craft.event.RegistryEvents;
import com.ultreon.craft.resources.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Registry<T> {
    private static final Map<Class<?>, Registry<?>> REGISTRIES = new HashMap<>();
    private static final AtomicBoolean CREATION_FROZEN = new AtomicBoolean();

    private final Class<T> type;
    private final Map<Identifier, T> map = new HashMap<>();
    private boolean frozen = false;

    public Registry(Class<T> type) {
        if (CREATION_FROZEN.get())
            throw new IllegalArgumentException("Registry creation is frozen!");
        if (REGISTRIES.containsKey(type))
            throw new IllegalArgumentException("Duplicate registry type: " + type.getName());
        REGISTRIES.put(type, this);
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public Registry(T... type) {
        this((Class<T>) type.getClass().getComponentType());
    }

    public static void freezeAll() {
        CREATION_FROZEN.set(true);
        REGISTRIES.values().forEach(Registry::freeze);
    }

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> get(Class<T> clazz) {
        return (Registry<T>) REGISTRIES.get(clazz);
    }

    public static void postEvents() {
        for (Registry<?> value : REGISTRIES.values()) {
            post(value);
        }
    }

    private static <T> void post(Registry<T> registry) {
        RegistryEvents.get(registry).factory().onRegister(registry);
    }

    public void set(Identifier id, T value) {
        if (this.frozen) throw new IllegalStateException("Registry is frozen!");
        this.map.put(id, value);
    }

    public T get(Identifier id) {
        return map.get(id);
    }

    public void freeze() {
        if (frozen) throw new IllegalStateException("Registry is already frozen");
        frozen = true;
    }

    public boolean contains(Identifier id) {
        return map.containsKey(id);
    }

    public Class<T> type() {
        return null;
    }

    public Collection<T> getValues() {
        return map.values();
    }

    public Set<Identifier> getKeys() {
        return map.keySet();
    }
}

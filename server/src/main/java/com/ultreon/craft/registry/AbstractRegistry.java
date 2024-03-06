package com.ultreon.craft.registry;

import java.util.*;

/**
 * Base registry.
 *
 * @param <K> The type key type for the registry.
 * @param <V> The type value type for the registry.
 */
public abstract class AbstractRegistry<K, V> {
    protected final HashMap<K, V> registry = new HashMap<>();

    protected AbstractRegistry() throws IllegalStateException {

    }

    public abstract V get(K obj);

    public abstract void register(K key, V val);

    public abstract List<V> values();

    public abstract List<K> keys();

    public abstract Set<Map.Entry<K, V>> entries() throws IllegalAccessException;

    public V random() {
        return this.random(new Random());
    }

    private V random(Random random) {
        return this.values().get(random.nextInt(this.values().size()));
    }
}

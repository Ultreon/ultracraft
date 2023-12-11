package com.ultreon.craft.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base registry.
 *
 * @param <K> The type key type for the registry.
 * @param <V> The type value type for the registry.
 */
public abstract class AbstractRegistry<K, V> {
    public static AbstractRegistry<?, ?> INSTANCE;

    protected final HashMap<K, V> registry = new HashMap<>();

    public AbstractRegistry() throws IllegalStateException {

    }

    public abstract V get(K obj);

    public abstract void register(K key, V val);

    public abstract Collection<V> values();

    public abstract Set<K> keys();

    public abstract Set<Map.Entry<K, V>> entries() throws IllegalAccessException;
}

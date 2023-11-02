package com.ultreon.craft.collection;

import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("SuspiciousMethodCalls")
public class OrderedMap<K, V> extends AbstractMap<K, V> {
    private final List<K> keys = new ArrayList<>();
    private final List<V> values = new ArrayList<>();

    @Override
    public @NotNull Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entrySet = new HashSet<>();
        this.validateSizes();
        for (int i = 0; i < this.keys.size(); i++) {
            K key = this.keys.get(i);
            V value = this.values.get(i);
            entrySet.add(new Entry<>() {
                @Override
                public K getKey() {
                    return key;
                }

                @Override
                public V getValue() {
                    return value;
                }

                @Override
                public V setValue(V value) {
                    throw new UnsupportedOperationException();
                }
            });
        }
        return entrySet;
    }

    @Override
    public V put(K key, V value) {
        this.validateSizes();
        if (this.containsKey(key)) {
            int index = this.keys.indexOf(key);
            V set = this.values.set(index, value);
            this.validateSizes();
            return set;
        } else {
            this.keys.add(key);
            this.values.add(value);
            this.validateSizes();
            return null;
        }
    }

    @Override
    public V get(Object key) {
        this.validateSizes();
        if (this.containsKey(key)) {
            int index = this.keys.indexOf(key);
            return this.values.get(index);
        }
        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        this.validateSizes();
        return this.keys.contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        this.validateSizes();
        return this.values.contains(value);
    }

    @Override
    public int size() {
        this.validateSizes();
        return this.keys.size();
    }

    @Override
    public boolean isEmpty() {
        this.validateSizes();
        return this.keys.isEmpty();
    }

    private void validateSizes() {
        if (this.keys.size() != this.values.size()) {
            throw new ConcurrentModificationException("keys.size() != values.size()");
        }
    }

    @Override
    public V remove(Object key) {
        if (this.containsKey(key)) {
            int index = this.keys.indexOf(key);
            this.keys.remove(index);
            return this.values.remove(index);
        }
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        this.keys.clear();
        this.values.clear();
    }

    @NotNull
    @Override
    @Deprecated
    public Set<K> keySet() {
        return Set.copyOf(this.keys);
    }

    public List<K> keyList() {
        return Collections.unmodifiableList(this.keys);
    }

    public List<V> valueList() {
        return Collections.unmodifiableList(this.values);
    }

    @NotNull
    @Override
    @Deprecated
    public Collection<V> values() {
        return this.values;
    }

    public int indexOf(K key) {
        return this.keys.indexOf(key);
    }

    public int indexOfValue(V value) {
        return this.values.indexOf(value);
    }

    public boolean removeEntry(int index) {
        this.validateSizes();
        if (index >= this.size()) throw new IndexOutOfBoundsException(index);
        this.keys.remove(index);
        this.values.remove(index);
        return true;
    }
}

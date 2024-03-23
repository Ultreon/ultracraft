package com.ultreon.craft.world.gen.feature;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Supplier;

@Unmodifiable
public class DefaultedArray<T> implements List<T> {
    private final List<T> list = new ArrayList<>();
    private final int size;
    private final Supplier<T> defaultValue;

    public DefaultedArray(T defaultValue, int size) {
        this.defaultValue = () -> defaultValue;
        this.size = size;
    }

    public DefaultedArray(Supplier<T> defaultValue, int size) {
        this.defaultValue = defaultValue;
        this.size = size;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0 || list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.list.contains(o);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this.list.iterator();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return this.list.toArray();
    }

    @NotNull
    @Override
    public <T1> T1 @NotNull [] toArray(@NotNull T1 @NotNull [] a) {
        return this.list.toArray(a);
    }

    @Override
    public boolean add(T t) {
        if (this.list.size() >= this.size) return false;

        return this.list.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return this.list.remove(o);
    }

    @Override
    @SuppressWarnings("SlowListContainsAll")
    public boolean containsAll(@NotNull Collection<?> c) {
        return this.list.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        if (c.size() + this.list.size() > this.size) return false;

        return this.list.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        if (c.size() + this.list.size() > this.size) return false;

        return this.list.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return this.list.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return this.list.retainAll(c);
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            list.set(i, null);
        }
    }

    @Override
    public T get(int index) {
        if (index >= size) throw new IndexOutOfBoundsException(index);
        if (index < 0) throw new IndexOutOfBoundsException(index);
        if (index >= list.size()) return this.defaultValue.get();

        T t = list.get(index);
        return t == null ? this.defaultValue.get() : t;
    }

    @Override
    public T set(int index, T element) {
        if (element == null) {
            list.set(index, this.defaultValue.get());
        }
        return list.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        if (this.list.size() >= this.size) return;

        this.list.add(index, element);
    }

    @Override
    public T remove(int index) {
        if (index >= this.size) throw new IndexOutOfBoundsException(index);
        if (index < 0) throw new IndexOutOfBoundsException(index);
        if (index >= this.list.size()) return null;

        return this.list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return this.list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.list.lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<@NotNull T> listIterator() {
        return this.list.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<@NotNull T> listIterator(int index) {
        return this.list.listIterator(index);
    }

    @NotNull
    @Override
    public DefaultedArray<@NotNull T> subList(int fromIndex, int toIndex) {
        DefaultedArray<@NotNull T> list = new DefaultedArray<>(this.defaultValue, toIndex - fromIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            list.add(this.list.get(i));
        }
        return list;
    }
}

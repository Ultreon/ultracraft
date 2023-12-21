package com.ultreon.craft.collection;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

class Palette<T> {
    private T defaultValue;
    private T[] values;

    @SuppressWarnings("unchecked")
    Palette(List<T> values, T defaultValue) {
        this.defaultValue = defaultValue;
        if (values.isEmpty()) throw new IllegalArgumentException("Palette cannot be empty");
        this.values = values.toArray((T[]) Array.newInstance(Palette.toClass(values.get(0).getClass()), 0));
    }

    private static Class<?> toClass(Type type) {
        if (type instanceof GenericArrayType arrayType)
            return Array.newInstance(Palette.toClass(arrayType.getGenericComponentType()), 0).getClass();

        return (Class<?>) type;
    }

    @SuppressWarnings("unchecked")
    Palette(Class<T> clazz) {
        this.values = (T[]) Array.newInstance(clazz, 0);
    }

    Palette(T value, T... typeGetter) {
        this.defaultValue = value;
        this.values = (T[]) Array.newInstance(typeGetter.getClass().getComponentType(), 1);
        this.values[0] = value;
    }

    Palette(T[] values) {
        if (values.length == 0) throw new IllegalArgumentException("Palette cannot be empty");
        this.defaultValue = values[0];
        this.values = values;
    }

    public int add(T value) {
        int index = this.values.length;
        this.values = Arrays.copyOf(this.values, index + 1);
        this.values[index] = value;
        return index;
    }

    public void remove(int index) {
        this.values = ArrayUtils.remove(this.values, index);
    }

    public int indexOf(T block) {
        return ArrayUtils.indexOf(this.values, block);
    }

    public T get(short index) {
        return this.values[index];
    }

    public void dispose() {
        this.values = null;
    }

    public T[] getData() {
        return this.values;
    }

    @SuppressWarnings("unchecked")
    public void clear(T value) {
        this.values = (T[]) Array.newInstance(value.getClass().getComponentType(), 1);
        this.values[0] = value;
    }
}

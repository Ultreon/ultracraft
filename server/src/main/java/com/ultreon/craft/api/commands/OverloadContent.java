package com.ultreon.craft.api.commands;

import com.google.common.base.Objects;

import java.util.function.Function;

public class OverloadContent {
    private String name;
    private int size;
    IndexedCommandSpecValues indexedValues;

    public OverloadContent(String name, int size, IndexedCommandSpecValues indexedValues) {
        this.name = name;
        this.size = size;
        this.indexedValues = indexedValues;
    }

    @Override
    public String toString() {
        return "(" + this.size + ": " + this.indexedValues + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OverloadContent that)) return false;
        return this.size == that.size &&
               Objects.equal(this.name, that.name) &&
               Objects.equal(this.indexedValues, that.indexedValues);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name, this.size, this.indexedValues);
    }

    public OverloadContent mapName(Function<String, String> function){
        return OverloadContent.of(function.apply(this.name), this.size, this.indexedValues);
    }

    public OverloadContent mapSize(Function<Integer, Integer> function) {
        return OverloadContent.of(this.name, function.apply(this.size), this.indexedValues);
    }

    public OverloadContent mapIndexedValues(Function<IndexedCommandSpecValues, IndexedCommandSpecValues> function) {
        return OverloadContent.of(this.name, this.size, function.apply(this.indexedValues));
    }

    public static OverloadContent of(String name, int size, IndexedCommandSpecValues indexedValues) {
        return new OverloadContent(name, size, indexedValues);
    }
}
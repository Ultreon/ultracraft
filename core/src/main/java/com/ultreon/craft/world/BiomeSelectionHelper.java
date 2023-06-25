package com.ultreon.craft.world;

import java.util.Objects;

public final class BiomeSelectionHelper {
    private final int index;
    private final float distance;

    public BiomeSelectionHelper(int index, float distance) {
        this.index = index;
        this.distance = distance;
    }

    public int index() {
        return index;
    }

    public float distance() {
        return distance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BiomeSelectionHelper) obj;
        return this.index == that.index &&
                Float.floatToIntBits(this.distance) == Float.floatToIntBits(that.distance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, distance);
    }

    @Override
    public String toString() {
        return "BiomeSelectionHelper[" +
                "index=" + index + ", " +
                "distance=" + distance + ']';
    }

}

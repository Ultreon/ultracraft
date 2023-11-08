package com.ultreon.craft.world.gen.biome;

import java.util.Objects;

public final class BiomeIndex {
    private final int index;
    private final double distance;

    public BiomeIndex(int index, double distance) {
        this.index = index;
        this.distance = distance;
    }

    public int index() {
        return index;
    }

    public double distance() {
        return distance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BiomeIndex that = (BiomeIndex) obj;
        return this.index == that.index &&
                Double.doubleToLongBits(this.distance) == Double.doubleToLongBits(that.distance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, distance);
    }

    @Override
    public String toString() {
        return "BiomeIndex[" +
                "index=" + index + ", " +
                "distance=" + distance + ']';
    }

}

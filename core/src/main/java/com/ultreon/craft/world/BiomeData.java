package com.ultreon.craft.world;

import com.ultreon.craft.world.gen.BiomeGenerator;

import java.util.Objects;

public final class BiomeData {
    private final float temperatureStartThreshold;
    private final float temperatureEndThreshold;
    private final BiomeGenerator biomeGen;

    BiomeData(float temperatureStartThreshold, float temperatureEndThreshold, BiomeGenerator biomeGen) {
        this.temperatureStartThreshold = temperatureStartThreshold;
        this.temperatureEndThreshold = temperatureEndThreshold;
        this.biomeGen = biomeGen;
    }

    public float temperatureStartThreshold() {
        return temperatureStartThreshold;
    }

    public float temperatureEndThreshold() {
        return temperatureEndThreshold;
    }

    public BiomeGenerator biomeGen() {
        return biomeGen;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BiomeData that = (BiomeData) obj;
        return Float.floatToIntBits(this.temperatureStartThreshold) == Float.floatToIntBits(that.temperatureStartThreshold) &&
                Float.floatToIntBits(this.temperatureEndThreshold) == Float.floatToIntBits(that.temperatureEndThreshold) &&
                Objects.equals(this.biomeGen, that.biomeGen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperatureStartThreshold, temperatureEndThreshold, biomeGen);
    }

    @Override
    public String toString() {
        return "BiomeData[" +
                "temperatureStartThreshold=" + temperatureStartThreshold + ", " +
                "temperatureEndThreshold=" + temperatureEndThreshold + ", " +
                "biomeGen=" + biomeGen + ']';
    }

}

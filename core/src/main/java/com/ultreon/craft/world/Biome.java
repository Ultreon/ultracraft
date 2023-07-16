package com.ultreon.craft.world;

import com.ultreon.craft.world.gen.BiomeGenerator;

import java.util.Objects;

public final class Biome {
    private final float temperatureStartThreshold;
    private final float temperatureEndThreshold;
    private final BiomeGenerator biomeGen;

    public Biome(float temperatureStartThreshold, float temperatureEndThreshold, BiomeGenerator biomeGen) {
        this.temperatureStartThreshold = temperatureStartThreshold;
        this.temperatureEndThreshold = temperatureEndThreshold;
        this.biomeGen = biomeGen;
    }

    public float temperatureStartThreshold() {
        return this.temperatureStartThreshold;
    }

    public float temperatureEndThreshold() {
        return this.temperatureEndThreshold;
    }

    public BiomeGenerator biomeGen() {
        return this.biomeGen;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Biome that = (Biome) obj;
        return Float.floatToIntBits(this.temperatureStartThreshold) == Float.floatToIntBits(that.temperatureStartThreshold) &&
                Float.floatToIntBits(this.temperatureEndThreshold) == Float.floatToIntBits(that.temperatureEndThreshold) &&
                Objects.equals(this.biomeGen, that.biomeGen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.temperatureStartThreshold, this.temperatureEndThreshold, this.biomeGen);
    }

    @Override
    public String toString() {
        return "Biome[" +
                "temperatureStartThreshold=" + this.temperatureStartThreshold + ", " +
                "temperatureEndThreshold=" + this.temperatureEndThreshold + ", " +
                "biomeGen=" + this.biomeGen + ']';
    }

    public void dispose() {
        this.biomeGen.dispose();;
    }
}

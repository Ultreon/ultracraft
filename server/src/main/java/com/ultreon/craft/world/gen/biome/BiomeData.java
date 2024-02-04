package com.ultreon.craft.world.gen.biome;

public record BiomeData(float temperatureStartThreshold, float temperatureEndThreshold, boolean isOcean, BiomeGenerator biomeGen) {
    @Override
    public String toString() {
        return "BiomeData{" +
                "temperatureStartThreshold=" + this.temperatureStartThreshold +
                ", temperatureEndThreshold=" + this.temperatureEndThreshold +
                ", biomeGen=" + this.biomeGen +
                '}';
    }
}

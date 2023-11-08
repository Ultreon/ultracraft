package com.ultreon.craft.world.gen.biome;

public record BiomeData(float temperatureStartThreshold, float temperatureEndThreshold, BiomeGenerator biomeGen) {
    @Override
    public String toString() {
        return "BiomeData{" +
                "temperatureStartThreshold=" + temperatureStartThreshold +
                ", temperatureEndThreshold=" + temperatureEndThreshold +
                ", biomeGen=" + biomeGen +
                '}';
    }
}

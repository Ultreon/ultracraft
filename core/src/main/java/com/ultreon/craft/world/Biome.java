package com.ultreon.craft.world;

import com.ultreon.craft.world.gen.BiomeGenerator;

public record Biome(float temperatureStartThreshold, float temperatureEndThreshold, BiomeGenerator biomeGen) {
}

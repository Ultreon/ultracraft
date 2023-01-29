package com.ultreon.craft.world;

import com.ultreon.craft.world.gen.BiomeGenerator;

public record BiomeData(float temperatureStartThreshold, float temperatureEndThreshold, BiomeGenerator biomeGen) {
}

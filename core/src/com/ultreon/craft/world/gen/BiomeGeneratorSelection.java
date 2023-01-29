package com.ultreon.craft.world.gen;

import com.ultreon.craft.world.gen.BiomeGenerator;
import org.jetbrains.annotations.Nullable;

public class BiomeGeneratorSelection {
    public BiomeGenerator biomeGenerator;
    @Nullable
    public Integer terrainSurfaceNoise;

    public BiomeGeneratorSelection(BiomeGenerator biomeGenerator) {
        this(biomeGenerator, null);
    }

    public BiomeGeneratorSelection(BiomeGenerator biomeGenerator, @Nullable Integer terrainSurfaceNoise) {
        this.biomeGenerator = biomeGenerator;
        this.terrainSurfaceNoise = terrainSurfaceNoise;
    }
}
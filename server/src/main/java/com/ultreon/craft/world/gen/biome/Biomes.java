package com.ultreon.craft.world.gen.biome;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.gen.feature.RockFeature;
import com.ultreon.craft.world.gen.feature.TreeFeature;
import com.ultreon.craft.world.gen.layer.*;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseConfigs;
import com.ultreon.libs.commons.v0.Identifier;

public class Biomes {
    public static final Biome VOID = Biomes.register("void", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureStart(Float.NEGATIVE_INFINITY)
            .temperatureEnd(Float.POSITIVE_INFINITY)
            .doesNotGenerate()
            .build());

    public static final Biome PLAINS = Biomes.register("plains", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureStart(-2f)
            .temperatureEnd(0.5f)
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
            .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
            .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
            .feature(new PatchFeature(NoiseConfigs.PATCH, Blocks.DIRT, -0.5f, 4))
            .feature(new RockFeature(NoiseConfigs.ROCK, Blocks.STONE, 0.0005f))
            .feature(new TreeFeature(NoiseConfigs.TREE, Blocks.LOG, Blocks.LEAVES, 0.007f, 3, 5))
            .build());

    public static final Biome FOREST = Biomes.register("forest", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureStart(0.5f)
            .temperatureEnd(1f)
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
            .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
            .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
            .feature(new PatchFeature(NoiseConfigs.PATCH, Blocks.DIRT, -0.5f, 4))
            .feature(new RockFeature(NoiseConfigs.ROCK, Blocks.STONE, 0.0005f))
            .feature(new TreeFeature(NoiseConfigs.TREE, Blocks.LOG, Blocks.LEAVES, 0.04f, 3, 5))
            .build());

    public static final Biome DESERT = Biomes.register("desert", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureStart(1f)
            .temperatureEnd(2f)
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 7))
            .layer(new GroundTerrainLayer(Blocks.SANDSTONE, 3, 4))
            .layer(new SurfaceTerrainLayer(Blocks.SAND, 3))
            .feature(new PatchFeature(NoiseConfigs.PATCH, Blocks.SANDSTONE, -0.9f, 4))
            .build());

    public static final Biome OCEAN = Biomes.register("ocean", Biome.builder()
            .noise(NoiseConfigs.GENERIC_NOISE)
            .domainWarping(seed -> new DomainWarping(UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureStart(-2f)
            .temperatureEnd(2f)
            .layer(new SurfaceTerrainLayer(Blocks.DIRT, 4))
            .feature(new PatchFeature(NoiseConfigs.WATER_PATCH_1, Blocks.SAND, -0.3f, 4))
            .feature(new PatchFeature(NoiseConfigs.WATER_PATCH_2, Blocks.GRAVEL, -0.3f, 4))
            .ocean()
            .build());

    private static Biome register(String name, Biome biome) {
        Registries.BIOME.register(new Identifier(name), biome);
        return biome;
    }

    public static void nopInit() {
        // NOOP
    }
}

package com.ultreon.craft.world.gen.biome;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.gen.layer.*;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseConfigs;
import com.ultreon.libs.commons.v0.Identifier;

public class Biomes {
    public static final Biome PLAINS = Biomes.register("plains", Biome.builder()
            .noise(NoiseConfigs.PLAINS)
            .domainWarping(seed -> new DomainWarping(UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureStart(-2f)
            .temperatureEnd(2f)
            .layer(new WaterTerrainLayer(64))
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
            .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
            .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
            .extraLayer(new PatchTerrainLayer(NoiseConfigs.STONE_PATCH, Blocks.STONE))
            .build());

    public static final Biome DESERT = Biomes.register("desert", Biome.builder()
            .noise(NoiseConfigs.PLAINS)
            .domainWarping(seed -> new DomainWarping(UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureStart(-2f)
            .temperatureEnd(2f)
            .layer(new WaterTerrainLayer(64))
            .layer(new AirTerrainLayer())
            .layer(new UndergroundTerrainLayer(Blocks.STONE, 7))
            .layer(new GroundTerrainLayer(Blocks.SANDSTONE, 3, 4))
            .layer(new SurfaceTerrainLayer(Blocks.SAND, 3))
            .extraLayer(new PatchTerrainLayer(NoiseConfigs.STONE_PATCH, Blocks.STONE))
            .build());

    private static Biome register(String name, Biome biome) {
        Registries.BIOMES.register(new Identifier(name), biome);
        return biome;
    }

    public static void nopInit() {
        for (Biome biome : Registries.BIOMES.values()) {
            biome.buildLayers();
        }
    }
}

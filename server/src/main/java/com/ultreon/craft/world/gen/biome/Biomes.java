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
            .decorator(new WaterProcessor())
            .decorator(new AirProcessor())
            .decorator(new UndergroundProcessor(Blocks.STONE, 4))
            .decorator(new GroundProcessor(Blocks.DIRT, 1, 3))
            .decorator(new SurfaceProcessor(Blocks.GRASS_BLOCK, 0))
            .postDecorator(new PatchProcessor(NoiseConfigs.STONE_PATCH, Blocks.STONE))
            .build());

    public static final Biome DESERT = Biomes.register("desert", Biome.builder()
            .noise(NoiseConfigs.PLAINS)
            .domainWarping(seed -> new DomainWarping(UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed))))
            .temperatureStart(-2f)
            .temperatureEnd(2f)
            .decorator(new WaterProcessor())
            .decorator(new AirProcessor())
            .decorator(new UndergroundProcessor(Blocks.STONE, 7))
            .decorator(new GroundProcessor(Blocks.SANDSTONE, 3, 4))
            .decorator(new SurfaceProcessor(Blocks.SAND, 3))
            .postDecorator(new PatchProcessor(NoiseConfigs.STONE_PATCH, Blocks.STONE))
            .build());

    private static Biome register(String name, Biome biome) {
        Registries.BIOME.register(new Identifier(name), biome);
        return biome;
    }

    public static void nopInit() {
        for (Biome biome : Registries.BIOME.values()) {
            biome.buildDecorators();
        }
    }
}

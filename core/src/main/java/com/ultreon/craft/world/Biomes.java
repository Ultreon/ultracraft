package com.ultreon.craft.world;

import com.google.common.collect.Range;
import com.ultreon.craft.world.gen.GenerationStage;
import com.ultreon.craft.world.gen.feature.TreeFeature;
import com.ultreon.craft.world.gen.layer.*;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;

import java.util.ArrayList;
import java.util.List;

public class Biomes {
    public static List<Biome> buildBiomes(long seed) {
        DomainWarping domainWarping = new DomainWarping(NoiseSettingsInit.DOMAIN_X.create(seed), NoiseSettingsInit.DOMAIN_Y.create(seed));

        List<Biome> list = new ArrayList<>();
        list.add(new Biome.Builder()
                .noise(NoiseSettingsInit.DEFAULT)
                .domainWarping((v) -> domainWarping)
                .layer(new WaterTerrainLayer(64))
                .layer(new AirTerrainLayer())
                .layer(new SurfaceTerrainLayer())
                .layer(new StoneTerrainLayer())
                .layer(new UndergroundTerrainLayer())
//			.feature(new TreeFeature(GenerationStage.TREES, Range.open(3, 5)))
                .extraLayer(new StonePatchTerrainLayer(NoiseSettingsInit.STONE_PATCH.create(seed), domainWarping))
                .feature(new TreeFeature(GenerationStage.TREES, Range.open(3, 5)))
                .build());

        return list;
    }
}

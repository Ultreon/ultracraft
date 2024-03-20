package com.ultreon.craft.events;

import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.gen.WorldGenFeature;
import com.ultreon.craft.world.gen.layer.TerrainLayer;

import java.util.List;

public class WorldLifecycleEvents {
    public static final Event<WorldLifecycleEvents.BiomeLayersBuilt> BIOME_LAYERS_BUILT = Event.create();

    @FunctionalInterface
    public interface BiomeLayersBuilt {
        void onBiomeLayersBuilt(Biome biome, List<TerrainLayer> layers, List<WorldGenFeature> features);
    }
}

package com.ultreon.craft.events;

import com.ultreon.craft.events.api.Event;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.gen.WorldGenFeature;

import java.util.List;

public class WorldLifecycleEvent {
    public static final Event<WorldLifecycleEvent.BiomeLayersBuilt> BIOME_LAYERS_BUILT = Event.create();

    @FunctionalInterface
    public interface BiomeLayersBuilt {
        void onBiomeLayersBuilt(Biome biome, List<WorldGenFeature> features);
    }
}

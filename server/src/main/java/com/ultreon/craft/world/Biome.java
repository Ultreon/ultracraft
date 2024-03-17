package com.ultreon.craft.world;

import com.google.common.base.Preconditions;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.events.WorldLifecycleEvents;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.gen.WorldGenFeature;
import com.ultreon.craft.world.gen.biome.BiomeGenerator;
import com.ultreon.craft.world.gen.layer.TerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseConfig;
import com.ultreon.craft.world.gen.noise.NoiseConfigs;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.data.types.MapType;
import it.unimi.dsi.fastutil.longs.Long2ReferenceFunction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class Biome {
    private final NoiseConfig settings;
    private final List<TerrainLayer> layers = new ArrayList<>();
    private final List<WorldGenFeature> features = new ArrayList<>();
    private final float temperatureStart;
    private final float temperatureEnd;
    private final boolean isOcean;
    private final boolean doesNotGenerate;

    protected Biome(NoiseConfig settings, float temperatureStart, float temperatureEnd, boolean isOcean, boolean doesNotGenerate) {
        this.settings = settings;
        this.temperatureStart = temperatureStart;
        this.temperatureEnd = temperatureEnd;
        this.isOcean = isOcean;
        this.doesNotGenerate = doesNotGenerate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final void buildLayers() {
        this.onBuildLayers(this.layers, this.features);

        WorldLifecycleEvents.BIOME_LAYERS_BUILT.factory().onBiomeLayersBuilt(this, this.layers, this.features);
    }

    protected abstract void onBuildLayers(List<TerrainLayer> layers, List<WorldGenFeature> features);

    public boolean doesNotGenerate() {
        return this.doesNotGenerate;
    }

    public BiomeGenerator create(ServerWorld world, long seed) {
        NoiseInstance noiseInstance = this.settings.create(seed);
        WorldEvents.CREATE_BIOME.factory().onCreateBiome(world, noiseInstance, world.getTerrainGenerator().getLayerDomain(), this.layers, this.features);

        this.layers.forEach(layer -> layer.create(world));
        this.features.forEach(layer -> layer.create(world));

        DomainWarping domainWarping = new DomainWarping(UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed)));

        return new BiomeGenerator(world, this, noiseInstance, domainWarping, this.layers, this.features);
    }

    public NoiseConfig getSettings() {
        return this.settings;
    }

    public float getTemperatureStart() {
        return this.temperatureStart;
    }

    public float getTemperatureEnd() {
        return this.temperatureEnd;
    }

    public MapType save() {
        MapType mapType = new MapType();
        mapType.putString("id", String.valueOf(this.getId()));
        return mapType;
    }

    private Identifier getId() {
        return Registries.BIOME.getId(this);
    }

    public static Biome load(MapType mapType) {
        return Registries.BIOME.get(Identifier.tryParse(mapType.getString("id", "plains")));
    }

    public boolean isOcean() {
        return this.isOcean;
    }

    public static class Builder {
        @Nullable
        private NoiseConfig biomeNoise;
        private final List<TerrainLayer> layers = new ArrayList<>();
        private final List<WorldGenFeature> features = new ArrayList<>();
        private float temperatureStart = Float.NaN;
        private float temperatureEnd = Float.NaN;
        private boolean isOcean;
        private boolean doesNotGenerate;

        private Builder() {

        }

        public Builder noise(NoiseConfig biomeNoise) {
            this.biomeNoise = biomeNoise;
            return this;
        }

        public Builder domainWarping(Long2ReferenceFunction<DomainWarping> domainWarping) {
            return this;
        }

        public Builder layer(TerrainLayer layer) {
            this.layers.add(layer);
            return this;
        }

        public Builder feature(WorldGenFeature feature) {
            this.features.add(feature);
            return this;
        }

        public Builder temperatureStart(float temperatureStart) {
            this.temperatureStart = temperatureStart;
            return this;
        }

        public Builder temperatureEnd(float temperatureEnd) {
            this.temperatureEnd = temperatureEnd;
            return this;
        }

        public Builder ocean() {
            this.isOcean = true;
            return this;
        }

        public Biome build() {
            Preconditions.checkNotNull(this.biomeNoise, "Biome noise not set.");

            if (Float.isNaN(this.temperatureStart)) throw new IllegalArgumentException("Temperature start not set.");
            if (Float.isNaN(this.temperatureEnd)) throw new IllegalArgumentException("Temperature end not set.");

            return new Biome(this.biomeNoise, this.temperatureStart, this.temperatureEnd, this.isOcean, this.doesNotGenerate) {
                @Override
                protected void onBuildLayers(List<TerrainLayer> layerList, List<WorldGenFeature> featureList) {
                    layerList.addAll(Builder.this.layers);
                    featureList.addAll(Builder.this.features);
                }
            };
        }

        public Builder doesNotGenerate() {
            this.doesNotGenerate = true;
            return this;
        }
    }
}

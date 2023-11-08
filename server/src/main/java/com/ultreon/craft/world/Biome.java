package com.ultreon.craft.world;

import com.google.common.base.Preconditions;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.world.gen.biome.BiomeGenerator;
import com.ultreon.craft.world.gen.layer.TerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseConfig;
import com.ultreon.craft.world.gen.noise.NoiseConfigs;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import it.unimi.dsi.fastutil.longs.Long2ReferenceFunction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class Biome {
    private final NoiseConfig settings;
    private final List<TerrainLayer> layers = new ArrayList<>();
    private final List<TerrainLayer> extraLayers = new ArrayList<>();
    private final float temperatureStart;
    private final float temperatureEnd;

    public Biome(NoiseConfig settings, float temperatureStart, float temperatureEnd) {
        this.settings = settings;
        this.temperatureStart = temperatureStart;
        this.temperatureEnd = temperatureEnd;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final void buildLayers() {
        this.onBuildLayers(this.layers, this.extraLayers);
    }

    protected abstract void onBuildLayers(List<TerrainLayer> layers, List<TerrainLayer> extraLayers);

    public BiomeGenerator create(ServerWorld world, long seed) {
        NoiseInstance noiseInstance = this.settings.create(seed);
        WorldEvents.CREATE_BIOME.factory().onCreateBiome(world, noiseInstance, world.getTerrainGenerator().getLayerDomain(), this.layers, this.extraLayers);

        this.layers.forEach(layer -> layer.create(world));
        this.extraLayers.forEach(layer -> layer.create(world));

        DomainWarping domainWarping = new DomainWarping(UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed)));

        return new BiomeGenerator(world, this, noiseInstance, domainWarping, this.layers, this.extraLayers);
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
        return Registries.BIOMES.getKey(this);
    }

    public static Biome load(MapType mapType) {
        return Registries.BIOMES.getValue(Identifier.tryParse(mapType.getString("id", "plains")));
    }

    public static class Builder {
        @Nullable
        private NoiseConfig biomeNoise;
        private final List<TerrainLayer> layers = new ArrayList<>();
        private final List<TerrainLayer> extraLayers = new ArrayList<>();
        private float temperatureStart = Float.NaN;
        private float temperatureEnd = Float.NaN;

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

        public Builder extraLayer(TerrainLayer layer) {
            this.extraLayers.add(layer);
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

        public Biome build() {
            Preconditions.checkNotNull(this.biomeNoise, "Biome noise not set.");

            if (Float.isNaN(this.temperatureStart)) throw new IllegalArgumentException("Temperature start not set.");
            if (Float.isNaN(this.temperatureEnd)) throw new IllegalArgumentException("Temperature end not set.");

            return new Biome(this.biomeNoise, this.temperatureStart, this.temperatureEnd) {
                @Override
                protected void onBuildLayers(List<TerrainLayer> layerList, List<TerrainLayer> extraLayerList) {
                    layerList.addAll(Builder.this.layers);
                    extraLayerList.addAll(Builder.this.extraLayers);
                }
            };
        }
    }
}

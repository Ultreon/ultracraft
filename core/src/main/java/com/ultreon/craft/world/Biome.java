package com.ultreon.craft.world;

import com.google.common.base.Preconditions;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.world.gen.BiomeGenerator;
import com.ultreon.craft.world.gen.layer.TerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.craft.world.gen.noise.NoiseSettings;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.longs.Long2ReferenceFunction;
import org.jetbrains.annotations.Nullable;

public abstract class Biome {
    private final NoiseSettings settings;
    private final Long2ReferenceFunction<DomainWarping> domainWarping;
    private final List<TerrainLayer> layers = new ArrayList<>();
    private final List<TerrainLayer> extraLayers = new ArrayList<>();

    public Biome(NoiseSettings settings, Long2ReferenceFunction<DomainWarping> domainWarping) {
        this.settings = settings;
        this.domainWarping = domainWarping;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final void buildLayers() {
        this.onBuildLayers(this.layers, this.extraLayers);
    }

    protected abstract void onBuildLayers(List<TerrainLayer> layers, List<TerrainLayer> extraLayers);

    public BiomeGenerator create(World world, long seed) {
        NoiseInstance noiseInstance = this.settings.create(seed);
        WorldEvents.CREATE_BIOME.factory().onCreateBiome(world, noiseInstance, this.domainWarping.get(seed), this.layers, this.extraLayers);
        return new BiomeGenerator(world, noiseInstance, this.domainWarping.get(seed), this.layers, this.extraLayers);
    }

    public NoiseSettings getSettings() {
        return this.settings;
    }

    public static class Builder {
        @Nullable
        private NoiseSettings biomeNoise;
        @Nullable
        private Long2ReferenceFunction<DomainWarping> domainWarping;
        private final List<TerrainLayer> layers = new ArrayList<>();
        private final List<TerrainLayer> extraLayers = new ArrayList<>();

        public Builder noise(NoiseSettings biomeNoise) {
            this.biomeNoise = biomeNoise;
            return this;
        }

        public Builder domainWarping(Long2ReferenceFunction<DomainWarping> domainWarping) {
            this.domainWarping = domainWarping;
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

        public Biome build() {
            Preconditions.checkNotNull(this.biomeNoise, "Biome noise not set.");
            Preconditions.checkNotNull(this.domainWarping, "Domain warping not set.");

            return new Biome(this.biomeNoise, this.domainWarping) {
                @Override
                protected void onBuildLayers(List<TerrainLayer> layerList, List<TerrainLayer> extraLayerList) {
                    layerList.addAll(Biome.Builder.this.layers);
                    extraLayerList.addAll(Biome.Builder.this.extraLayers);
                }
            };
        }
    }
}

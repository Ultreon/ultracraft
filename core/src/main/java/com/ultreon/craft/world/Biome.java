package com.ultreon.craft.world;

import com.google.common.base.Preconditions;
import com.ultreon.craft.world.gen.BiomeGenerator;
import com.ultreon.craft.world.gen.layer.TerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseSettings;
import it.unimi.dsi.fastutil.longs.Long2ReferenceFunction;

import java.util.ArrayList;
import java.util.List;

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
        return new BiomeGenerator(world, this.settings.create(seed), this.domainWarping.get(seed), this.layers, this.extraLayers);
    }

    public NoiseSettings getSettings() {
        return this.settings;
    }

    public static class Builder {
        private NoiseSettings biomeNoise;
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

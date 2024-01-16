package com.ultreon.craft.world;

import com.google.common.base.Preconditions;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.events.WorldLifecycleEvent;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.world.gen.WorldGenFeature;
import com.ultreon.craft.world.gen.biome.BiomeGenerator;
import com.ultreon.craft.world.gen.layer.TerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseConfig;
import com.ultreon.craft.world.gen.noise.NoiseConfigs;
import com.ultreon.data.types.MapType;

import org.checkerframework.common.reflection.qual.NewInstance;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a biome.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 * @see BiomeGenerator
 */
public abstract class Biome {
    private final NoiseConfig config;
    private final List<TerrainLayer> layers = new ArrayList<>();
    private final List<WorldGenFeature> features = new ArrayList<>();
    private final float temperatureStart;
    private final float temperatureEnd;
    private final boolean isOcean;
    private final boolean doesNotGenerate;

    /**
     * Create a new biome
     * 
     * @param config           the noise configuration to use
     * @param temperatureStart the start temperature of the biome
     * @param temperatureEnd   the end temperature of the biome
     * @param isOcean          whether the biome is an ocean
     * @param doesNotGenerate  whether the biome does not generate naturally
     */
    protected Biome(NoiseConfig config, float temperatureStart, float temperatureEnd, boolean isOcean, boolean doesNotGenerate) {
        this.config = config;
        this.temperatureStart = temperatureStart;
        this.temperatureEnd = temperatureEnd;
        this.isOcean = isOcean;
        this.doesNotGenerate = doesNotGenerate;
    }

    /**
     * Create a new biome builder.
     * 
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Build the biome layers and features information
     * 
     * @see WorldLifecycleEvent#BIOME_LAYERS_BUILT
     */
    @Internal
    public final void buildLayers() {
        this.onBuildLayers(this.layers, this.features);

        WorldLifecycleEvent.BIOME_LAYERS_BUILT.factory().onBiomeLayersBuilt(this, this.features);
    }

    /**
     * Called when the biome information is being built
     * 
     * @param layers   the terrain layers to add
     * @param features the features to add
     * @see #buildLayers()
     */
    protected abstract void onBuildLayers(List<TerrainLayer> layers, List<WorldGenFeature> features);

    /**
     * Check if the biome does not generate naturally.
     * 
     * @return true if the biome does not generate, false otherwise
     */
    public boolean doesNotGenerate() {
        return this.doesNotGenerate;
    }

    /**
     * Create a new {@link BiomeGenerator} for the given world.
     * 
     * @param world the world to create the generator for
     * @param seed  the seed to use
     * @return      the biome generator
     */
    public BiomeGenerator create(ServerWorld world, long seed) {
        var noiseInstance = this.config.create(seed);
        WorldEvents.CREATE_BIOME.factory().onCreateBiome(world, noiseInstance, world.getTerrainGenerator().getLayerDomain(), this.layers, this.features);

        this.layers.forEach(layer -> layer.create(world));
        this.features.forEach(layer -> layer.create(world));

        DomainWarping domainWarping = new DomainWarping(UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed)));

        return new BiomeGenerator(world, this, noiseInstance, domainWarping, this.layers, this.features);
    }

    /**
     * Get the noise configuration of the biome
     * 
     * @return the noise configuration
     */
    public NoiseConfig getConfig() {
        return this.config;
    }

    /**
     * Get the start temperature of the biome
     * 
     * @return the start temperature
     */
    public float getTemperatureStart() {
        return this.temperatureStart;
    }

    /**
     * Get the end temperature of the biome
     * 
     * @return the end temperature
     */
    public float getTemperatureEnd() {
        return this.temperatureEnd;
    }

    /**
     * Saves the biome to UBO data.
     * 
     * @return the output data
     */
    public MapType save() {
        MapType mapType = new MapType();
        mapType.putString("id", String.valueOf(this.getId()));
        return mapType;
    }

    /**
     * Get the element id of the biome.
     * 
     * @return the element id
     */
    private ElementID getId() {
        return Registries.BIOME.getKey(this);
    }

    /**
     * Loads a biome from UBO data.
     * 
     * @param mapType the input data
     * @return the loaded biome
     */
    public static Biome load(MapType mapType) {
        return Registries.BIOME.getValue(ElementID.tryParse(mapType.getString("id", "plains")));
    }

    /**
     * Determines if the biome is an ocean
     * 
     * @return true if the biome is an ocean, false otherwise
     */
    public boolean isOcean() {
        return this.isOcean;
    }

    /**
     * Builder for {@link Biome}
     * 
     * @author <a href="https://github.com/XyperCode">XyperCode</a>
     * @since 0.1.0
     * @see Biome
     */
    public static class Builder {
        @Nullable
        private NoiseConfig biomeNoise;
        private final List<TerrainLayer> layers = new ArrayList<>();
        private final List<WorldGenFeature> features = new ArrayList<>();
        private float temperatureStart = Float.NaN;
        private float temperatureEnd = Float.NaN;
        private boolean isOcean;
        private boolean doesNotGenerate;

        private boolean invalid = false;

        private Builder() {

        }

        /**
         * The noice configuration to use for the biome.
         * 
         * @param biomeNoise the noise configuration
         * @return this
         */
        public @This Builder noise(NoiseConfig biomeNoise) {
            if (this.invalid) throw new IllegalStateException("Cannot set noise after build.");

            this.biomeNoise = biomeNoise;
            return this;
        }

        /**
         * @param domainWarping the domain warping to use
         * @return this
         */
        @Deprecated(since = "0.1.0", forRemoval = true)
        public @This Builder domainWarping(SeededSupplier<DomainWarping> domainWarping) {
            if (this.invalid) throw new IllegalStateException("Cannot set domain warping after build.");

            CommonConstants.LOGGER.warn("The domain warping for biomes is deprecated and will be removed in the future, currently no domain warping can be set.");
            return this;
        }

        /**
         * Add a terrain layer to the biome
         * 
         * @param layer the terrain layer
         * @return this
         */
        public @This Builder layer(TerrainLayer layer) {
            if (this.invalid) throw new IllegalStateException("Cannot add layers after build.");

            this.layers.add(layer);
            return this;
        }

        /**
         * Add a feature to the biome
         * 
         * @param feature the feature
         * @return this
         */
        public @This Builder feature(WorldGenFeature feature) {
            if (this.invalid) throw new IllegalStateException("Cannot add features after build.");

            this.features.add(feature);
            return this;
        }

        /**
         * Set the start temperature of the biome
         * 
         * @param temperatureStart the start temperature
         * @return this
         */
        public @This Builder temperatureStart(float temperatureStart) {
            if (this.invalid) throw new IllegalStateException("Cannot set start temperature after build.");

            this.temperatureStart = temperatureStart;
            return this;
        }

        /**
         * Set the end temperature of the biome
         * 
         * @param temperatureEnd the end temperature
         * @return this
         */
        public @This Builder temperatureEnd(float temperatureEnd) {
            if (this.invalid) throw new IllegalStateException("Cannot set end temperature after build.");

            this.temperatureEnd = temperatureEnd;
            return this;
        }

        /**
         * Set the biome to be an ocean
         * 
         * @return this
         */
        public @This Builder ocean() {
            if (this.invalid) throw new IllegalStateException("Cannot set ocean after build.");

            this.isOcean = true;
            return this;
        }
        
        /**
         * Set the biome to not generate
         * 
         * @return this
         */
        public @This Builder doesNotGenerate() {
            if (this.invalid) throw new IllegalStateException("Cannot set doesNotGenerate after build.");

            this.doesNotGenerate = true;
            return this;
        }

        /**
         * Build the biome with the current settings
         * 
         * @return the biome
         */
        public @NewInstance Biome build() {
            if (this.invalid) throw new IllegalStateException("Cannot build biome after build.");

            Preconditions.checkNotNull(this.biomeNoise, "Biome noise not set.");

            this.invalid = true;

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
    }
}

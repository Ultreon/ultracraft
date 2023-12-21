package com.ultreon.craft.world;

import com.google.common.base.Preconditions;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.world.gen.biome.BiomeGenerator;
import com.ultreon.craft.world.gen.layer.Decorator;
import com.ultreon.craft.world.gen.layer.VoidguardProcessor;
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
    private final List<Decorator> decorators = new ArrayList<>();
    private final List<Decorator> postDecorator = new ArrayList<>();
    private final float temperatureStart;
    private final float temperatureEnd;

    protected Biome(NoiseConfig settings, float temperatureStart, float temperatureEnd) {
        this.settings = settings;
        this.temperatureStart = temperatureStart;
        this.temperatureEnd = temperatureEnd;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final void buildDecorators() {
        this.onBuildDecorators(this.decorators, this.postDecorator);
    }

    protected abstract void onBuildDecorators(List<Decorator> decorators, List<Decorator> postDecorators);

    public BiomeGenerator create(ServerWorld world, long seed) {
        NoiseInstance noiseInstance = this.settings.create(seed);
        WorldEvents.CREATE_BIOME.factory().onCreateBiome(world, noiseInstance, world.getTerrainGenerator().getLayerDomain(), this.decorators, this.postDecorator);

        this.decorators.forEach(decorator -> decorator.create(world));
        this.postDecorator.forEach(decorator -> decorator.create(world));

        DomainWarping domainWarping = new DomainWarping(UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_X.create(seed)), UltracraftServer.get().disposeOnClose(NoiseConfigs.LAYER_Y.create(seed)));

        return new BiomeGenerator(world, this, noiseInstance, domainWarping, this.decorators, this.postDecorator);
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

    public Identifier getId() {
        return Registries.BIOME.getKey(this);
    }

    public static Biome load(MapType mapType) {
        return Registries.BIOME.getValue(Identifier.tryParse(mapType.getString("id", "plains")));
    }

    public static class Builder {
        @Nullable
        private NoiseConfig biomeNoise;
        private final List<Decorator> decorators = new ArrayList<>();
        private final List<Decorator> postDecorators = new ArrayList<>();
        private float temperatureStart = Float.NaN;
        private float temperatureEnd = Float.NaN;

        private Builder() {

        }

        /**
         * Set biome noise configuration.
         *
         * @param biomeNoise the biome noise configuration
         * @return this
         */
        public Builder noise(NoiseConfig biomeNoise) {
            this.biomeNoise = biomeNoise;
            return this;
        }

        /**
         * Set domain warping function.
         *
         * @param domainWarping the domain warping function
         * @return this
         */
        public Builder domainWarping(Long2ReferenceFunction<DomainWarping> domainWarping) {
            return this;
        }

        /**
         * Adds a decorator to the biome.
         *
         * @param layer the decorator to add.
         * @return this
         */
        public Builder decorator(Decorator layer) {
            this.decorators.add(layer);
            return this;
        }

        /**
         * Post decorators are executed after all other decorators
         *
         * @param decorator the decorator to add.
         * @return this
         */
        public Builder postDecorator(Decorator decorator) {
            this.postDecorators.add(decorator);
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

            this.decorators.add(new VoidguardProcessor());

            return new Biome(this.biomeNoise, this.temperatureStart, this.temperatureEnd) {
                @Override
                protected void onBuildDecorators(List<Decorator> decorators, List<Decorator> postDecorators) {
                    decorators.addAll(Builder.this.decorators);
                    postDecorators.addAll(Builder.this.postDecorators);
                }
            };
        }
    }
}

package com.ultreon.craft.world.gen;

import com.ultreon.craft.util.Vec2i;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.layer.TerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.MyNoise;
import com.ultreon.craft.world.gen.noise.NoiseSettings;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BiomeGenerator {
    private final NoiseSettings biomeNoise;
    private final DomainWarping domainWarping;
    private final List<TerrainLayer> layers;
    private final List<TerrainLayer> extraLayers;
    public static final boolean USE_DOMAIN_WARPING = true;

    public BiomeGenerator(NoiseSettings biomeNoise, DomainWarping domainWarping, List<TerrainLayer> layers, List<TerrainLayer> extraLayers) {
        this.biomeNoise = biomeNoise;
        this.domainWarping = domainWarping;
        this.layers = layers;
        this.extraLayers = extraLayers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Chunk processColumn(Chunk chunk, int x, int z, Vec2i mapSeed, @Nullable Integer height) {
        biomeNoise.worldOffset = mapSeed;
        int groundPos;

        groundPos = Objects.requireNonNullElseGet(height, () -> getSurfaceHeightNoise(chunk.offset.x + x, chunk.offset.z + z, chunk.height));

        for (int y = (int) chunk.offset.y; y < (int) chunk.offset.y + (int) chunk.width; y++) {
            for (var layer : layers) {
                if (layer.handle(chunk, x, y, z, groundPos, mapSeed)) {
                    break;
                }
            }
        }

        for (var layer : extraLayers) {
            layer.handle(chunk, x, (int) chunk.offset.y, z, groundPos, mapSeed);
        }

        return chunk;
    }

    private int getSurfaceHeightNoise(float x, float z, int height) {
        float terrainHeight;
        if (!USE_DOMAIN_WARPING) {
            terrainHeight = MyNoise.octavePerlin(x, z, biomeNoise);
        } else {
            terrainHeight = domainWarping.GenerateDomainNoise((int) x, (int) z, biomeNoise);
        }

        terrainHeight = MyNoise.redistribution(terrainHeight, biomeNoise);
        return MyNoise.remapValue01ToInt(terrainHeight, 0, World.CHUNK_HEIGHT);
    }

    public static class Builder {
        private NoiseSettings biomeNoise;
        private DomainWarping domainWarping;
        private final List<TerrainLayer> layers = new ArrayList<>();
        private final List<TerrainLayer> extraLayers = new ArrayList<>();

        public Builder noise(NoiseSettings biomeNoise) {
            this.biomeNoise = biomeNoise;
            return this;
        }

        public Builder domainWarping(DomainWarping domainWarping) {
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

        public BiomeGenerator createBiomeGenerator() {
            return new BiomeGenerator(biomeNoise, domainWarping, layers, extraLayers);
        }
    }
}

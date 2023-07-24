package com.ultreon.craft.world.gen;

import com.badlogic.gdx.utils.Disposable;
import com.google.common.base.Preconditions;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.layer.TerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.MyNoise;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

public class BiomeGenerator implements Disposable {
    private final NoiseInstance biomeNoise;
    private final DomainWarping domainWarping;
    private final List<TerrainLayer> layers;
    private final List<TerrainLayer> extraLayers;
    public static final boolean USE_DOMAIN_WARPING = true;
    @UnknownNullability
    public TreeGenerator treeGenerator;

    public BiomeGenerator(NoiseInstance noise, DomainWarping domainWarping, List<TerrainLayer> layers, List<TerrainLayer> extraLayers) {
        this.biomeNoise = noise;
        this.domainWarping = domainWarping;
        this.layers = layers;
        this.extraLayers = extraLayers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Chunk processColumn(World world, Chunk chunk, int x, int z, long seed, @Nullable Integer height) {
        final int chunkAmplitude = 1;

        int groundPos = this.getSurfaceHeightNoise(chunk.getOffset().x + x, chunk.getOffset().z + z, chunk.height) * chunkAmplitude;

        for (int y = chunk.getOffset().y; y < chunk.getOffset().y + chunk.height; y++) {
            for (TerrainLayer layer : this.layers) {
                if (layer.handle(world, chunk, x, y, z, groundPos)) {
                    break;
                }
            }
        }

        for (TerrainLayer layer : this.extraLayers) {
            layer.handle(world, chunk, x, chunk.getOffset().y, z, groundPos);
        }

        return chunk;
    }

    public int getSurfaceHeightNoise(float x, float z, int height) {
        double terrainHeight;
        if (!USE_DOMAIN_WARPING) {
            terrainHeight = MyNoise.octavePerlin(x, z, this.biomeNoise);
        } else {
            terrainHeight = this.domainWarping.generateDomainNoise((int) x, (int) z, this.biomeNoise);
        }

        terrainHeight = MyNoise.redistribution(terrainHeight, this.biomeNoise);
        return MyNoise.remapValue01ToInt(terrainHeight, 0, height);
    }

    public TreeData getTreeData(Chunk chunk, long seed) {
        if (this.treeGenerator == null)
            return new TreeData();

        return this.treeGenerator.generateTreeData(chunk, seed);
    }

    @Override
    public void dispose() {
        this.biomeNoise.dispose();
        this.domainWarping.dispose();
    }

    public static class Builder {
        @Nullable
        private NoiseInstance biomeNoise;
        @Nullable
        private DomainWarping domainWarping;
        private final List<TerrainLayer> layers = new ArrayList<>();
        private final List<TerrainLayer> extraLayers = new ArrayList<>();

        public Builder noise(NoiseInstance biomeNoise) {
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

        public BiomeGenerator build() {
            Preconditions.checkNotNull(this.biomeNoise, "Biome noise has not set up");
            Preconditions.checkNotNull(this.domainWarping, "Domain warping has not set up");

            return new BiomeGenerator(this.biomeNoise, this.domainWarping, this.layers, this.extraLayers);
        }
    }
}

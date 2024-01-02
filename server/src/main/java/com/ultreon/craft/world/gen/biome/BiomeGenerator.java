package com.ultreon.craft.world.gen.biome;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.craft.world.*;
import com.ultreon.craft.world.gen.Carver;
import com.ultreon.craft.world.gen.TreeData;
import com.ultreon.craft.world.gen.TreeGenerator;
import com.ultreon.craft.world.gen.layer.TerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

public class BiomeGenerator implements ServerDisposable {
    private final World world;
    private final NoiseInstance biomeNoise;
    private final List<TerrainLayer> layers;
    private final List<TerrainLayer> extraLayers;
    public static final boolean USE_DOMAIN_WARPING = true;
    @UnknownNullability
    public TreeGenerator treeGenerator;
    private final Biome biome;
    private Carver carver;

    public BiomeGenerator(World world, Biome biome, NoiseInstance noise, DomainWarping domainWarping, List<TerrainLayer> layers, List<TerrainLayer> extraLayers) {
        this.world = world;
        this.biome = biome;
        this.biomeNoise = noise;
        this.carver = new Carver(domainWarping, noise);
        this.layers = layers;
        this.extraLayers = extraLayers;
    }

    public BuilderChunk processColumn(BuilderChunk chunk, int x, int z) {
        int groundPos = this.carver.carve(chunk, x, z);
        LightMap lightMap = chunk.getLightMap();

        for (int y = chunk.getOffset().y + 1; y < chunk.getOffset().y + chunk.height; y++) {
            if (chunk.get(x, y, z).isAir()) continue;

            for (var layer : this.layers) {
                if (layer.handle(this.world, chunk, x, y, z, groundPos)) {
                    break;
                }
            }
        }

        for (var layer : this.extraLayers) {
            layer.handle(this.world, chunk, x, chunk.getOffset().y, z, groundPos);
        }

        int highest = chunk.getHighest(x, z);
        for (int y = chunk.getOffset().y; y < chunk.getOffset().y + chunk.height; y++) {
            lightMap.setSunlight(x, y, z, y >= highest ? 15 : 7);
        }
        chunk.set(x, chunk.getOffset().y, z, Blocks.VOIDGUARD);

        return chunk;
    }

    public TreeData createTreeData(Chunk chunk) {
        if (this.treeGenerator == null)
            return new TreeData();

        return this.treeGenerator.generateTreeData(chunk);
    }

    @Override
    public void dispose() {
        this.biomeNoise.dispose();

        this.layers.forEach(TerrainLayer::dispose);
        this.extraLayers.forEach(TerrainLayer::dispose);
    }

    public World getWorld() {
        return this.world;
    }

    public Biome getBiome() {
        return this.biome;
    }

    public Carver getCarver() {
        return this.carver;
    }

    public static class Index {
        public BiomeGenerator biomeGenerator;
        @Nullable
        public Integer terrainSurfaceNoise;

        public Index(BiomeGenerator biomeGenerator) {
            this(biomeGenerator, null);
        }

        public Index(BiomeGenerator biomeGenerator, @Nullable Integer terrainSurfaceNoise) {
            this.biomeGenerator = biomeGenerator;
            this.terrainSurfaceNoise = terrainSurfaceNoise;
        }
    }
}

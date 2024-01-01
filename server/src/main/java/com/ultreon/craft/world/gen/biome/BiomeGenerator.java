package com.ultreon.craft.world.gen.biome;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.BuilderChunk;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.TreeData;
import com.ultreon.craft.world.gen.TreeGenerator;
import com.ultreon.craft.world.gen.layer.TerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.craft.world.gen.noise.NoiseUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

public class BiomeGenerator implements ServerDisposable {
    private final World world;
    private final NoiseInstance biomeNoise;
    private final DomainWarping domainWarping;
    private final List<TerrainLayer> layers;
    private final List<TerrainLayer> extraLayers;
    public static final boolean USE_DOMAIN_WARPING = true;
    @UnknownNullability
    public TreeGenerator treeGenerator;
    private final Biome biome;

    public BiomeGenerator(World world, Biome biome, NoiseInstance noise, DomainWarping domainWarping, List<TerrainLayer> layers, List<TerrainLayer> extraLayers) {
        this.world = world;
        this.biome = biome;
        this.biomeNoise = noise;
        this.domainWarping = domainWarping;
        this.layers = layers;
        this.extraLayers = extraLayers;
    }

    public BuilderChunk processColumn(BuilderChunk chunk, int x, int z) {
        final int chunkAmplitude = 1;

        int groundPos = this.getSurfaceHeightNoise(chunk.getOffset().x + x, chunk.getOffset().z + z) * chunkAmplitude;

        for (int y = chunk.getOffset().y + 1; y < chunk.getOffset().y + chunk.height; y++) {
            for (TerrainLayer layer : this.layers) {
                if (layer.handle(this.world, chunk, x, y, z, groundPos)) {
                    break;
                }
            }
        }


        for (TerrainLayer layer : this.extraLayers) {
            layer.handle(this.world, chunk, x, chunk.getOffset().y, z, groundPos);
        }

        chunk.set(x, chunk.getOffset().y, z, Blocks.VOIDGUARD);

        return chunk;
    }

    public int getSurfaceHeightNoise(float x, float z) {
        double height;

        if (BiomeGenerator.USE_DOMAIN_WARPING)
            height = this.domainWarping.generateDomainNoise((int) x, (int) z, this.biomeNoise);
        else
            height = NoiseUtils.octavePerlin(x, z, this.biomeNoise);

        return (int) Math.ceil(Math.max(height, 1));
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

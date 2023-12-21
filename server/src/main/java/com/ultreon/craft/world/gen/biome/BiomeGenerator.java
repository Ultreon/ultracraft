package com.ultreon.craft.world.gen.biome;

import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.craft.world.*;
import com.ultreon.craft.world.gen.TreeData;
import com.ultreon.craft.world.gen.TreeGenerator;
import com.ultreon.craft.world.gen.carver.Carver;
import com.ultreon.craft.world.gen.layer.Decorator;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.craft.world.gen.noise.NoiseUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BiomeGenerator implements ServerDisposable {
    public static final BiomeGenerator EMPTY = new EmptyBiomeGenerator();
    private final World world;
    private final NoiseInstance biomeNoise;
    private final DomainWarping domainWarping;
    private final List<Decorator> layers;
    private final List<Decorator> postDecorator;
    public static final boolean USE_DOMAIN_WARPING = true;
    @UnknownNullability
    public TreeGenerator treeGenerator;
    private final Biome biome;

    public BiomeGenerator(World world, Biome biome, NoiseInstance noise, DomainWarping domainWarping, List<Decorator> layers, List<Decorator> postDecorator) {
        this.world = world;
        this.biome = biome;
        this.biomeNoise = noise;
        this.domainWarping = domainWarping;
        this.layers = layers;
        this.postDecorator = postDecorator;
    }

    public BuilderChunk processColumn(BuilderChunk chunk, Carver carver, int x, int z) {
        int groundPos = this.getSurfaceHeightNoise(chunk.getOffset().x + x, chunk.getOffset().z + z) * chunk.getWorld().getWorldGenSettings().generationAmplitude();

        for (int y = -chunk.depth; y < chunk.height; y++) {
            carver.carve(chunk.getWorld(), chunk, chunk.getOffset().x + x, y, chunk.getOffset().z + z);
//            for (Decorator layer : this.layers) {
//                if (layer.handle(this.world, chunk, x, y, z, groundPos)) {
//                    break;
//                }
//            }
        }

//        for (Decorator decorator : this.postDecorator) {
//            decorator.handle(this.world, chunk, x, chunk.getOffset().y, z, groundPos);
//        }

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

        this.layers.forEach(Decorator::dispose);
        this.postDecorator.forEach(Decorator::dispose);
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

    private static class EmptyBiomeGenerator extends BiomeGenerator {
        public EmptyBiomeGenerator() {
            super(new EmptyWorld(), Biomes.PLAINS, NoiseInstance.ZERO, new DomainWarping(NoiseInstance.ZERO, NoiseInstance.ZERO), List.of(), List.of());
        }

        private static class EmptyWorld extends World {
            private static final Collection<? extends Chunk> EMPTY_LIST = Collections.emptyList();

            @Override
            protected int getRenderDistance() {
                return 0;
            }

            @Override
            protected boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkPos pos) {
                return false;
            }

            @Override
            protected void checkThread() {
                // Nothing
            }

            @Override
            public @Nullable Chunk getChunk(@NotNull ChunkPos pos) {
                return null;
            }

            @Override
            public Collection<? extends Chunk> getLoadedChunks() {
                return EmptyWorld.EMPTY_LIST;
            }

            @Override
            public boolean isClientSide() {
                return false;
            }
        }
    }
}

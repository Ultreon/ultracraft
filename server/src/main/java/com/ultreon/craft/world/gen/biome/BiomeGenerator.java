package com.ultreon.craft.world.gen.biome;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.debug.WorldGenDebugContext;
import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.craft.world.*;
import com.ultreon.craft.world.gen.*;
import com.ultreon.craft.world.gen.layer.TerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.List;

import static com.ultreon.craft.world.World.CHUNK_HEIGHT;

public class BiomeGenerator implements ServerDisposable {
    private final World world;
    private final NoiseInstance biomeNoise;
    private final List<TerrainLayer> layers;
    private final List<WorldGenFeature> features;
    public static final boolean USE_DOMAIN_WARPING = true;
    @UnknownNullability
    public TreeGenerator treeGenerator;
    private final Biome biome;
    private final Carver carver;

    public BiomeGenerator(World world, Biome biome, NoiseInstance noise, DomainWarping domainWarping, List<TerrainLayer> layers, List<WorldGenFeature> features) {
        this.world = world;
        this.biome = biome;
        this.biomeNoise = noise;
        this.carver = new Carver(domainWarping, noise);
        this.layers = layers;
        this.features = features;
    }

    public BuilderChunk processColumn(BuilderChunk chunk, int x, int z, Collection<ServerWorld.RecordedChange> recordedChanges) {
        int groundPos = this.carver.carve(chunk, x, z);
        LightMap lightMap = chunk.getLightMap();

        this.generateTerrainLayers(chunk, x, z, groundPos);

        BiomeGenerator.setRecordedChanges(chunk, x, z, recordedChanges);

        BiomeGenerator.updateLightMap(chunk, x, z, lightMap);
        chunk.set(x, chunk.getOffset().y, z, Blocks.VOIDGUARD);

        return chunk;
    }

    private static void updateLightMap(BuilderChunk chunk, int x, int z, LightMap lightMap) {
        int highest = chunk.getHighest(x, z);
        for (int y = chunk.getOffset().y; y < chunk.getOffset().y + CHUNK_HEIGHT; y++) {
            lightMap.setSunlight(x, y, z, y >= highest ? 15 : 7);
        }
    }

    public void generateTerrainFeatures(RecordingChunk chunk, int x, int z, int groundPos) {
        for (var feature : this.features) {
            feature.handle(this.world, chunk, x, z, groundPos);
        }
    }

    private void generateTerrainLayers(BuilderChunk chunk, int x, int z, int groundPos) {
        for (int y = chunk.getOffset().y + 1; y < chunk.getOffset().y + CHUNK_HEIGHT; y++) {
            if (chunk.get(x, y, z).isAir()) continue;

            for (var layer : this.layers) {
                if (layer.handle(this.world, chunk, x, y, z, groundPos)) {
                    break;
                }
            }
        }
    }

    private static void setRecordedChanges(BuilderChunk chunk, int x, int z, Collection<ServerWorld.RecordedChange> recordedChanges) {
        for (ServerWorld.RecordedChange recordedChange : recordedChanges) {
            boolean isWithinChunkBounds = recordedChange.x() >= chunk.getOffset().x && recordedChange.x() < chunk.getOffset().x + World.CHUNK_SIZE
                    && recordedChange.z() >= chunk.getOffset().z && recordedChange.z() < chunk.getOffset().z + World.CHUNK_SIZE;
            BlockPos localBlockPos = World.toLocalBlockPos(recordedChange.x(), recordedChange.y(), recordedChange.z());
            if (isWithinChunkBounds && localBlockPos.x() == x && localBlockPos.z() == z) {
                chunk.set(World.toLocalBlockPos(recordedChange.x(), recordedChange.y(), recordedChange.z()).vec(), recordedChange.block());
                if (WorldGenDebugContext.isActive()) {
                    System.out.println("[DEBUG CHUNK-HASH " + System.identityHashCode(chunk) + "] Setting recorded change in chunk at " + recordedChange.x() + ", " + recordedChange.y() + ", " + recordedChange.z() + " of type " + recordedChange.block());
                }
            }
        }
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
        this.features.forEach(WorldGenFeature::dispose);
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

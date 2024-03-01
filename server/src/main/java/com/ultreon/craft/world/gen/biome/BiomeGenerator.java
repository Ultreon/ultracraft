package com.ultreon.craft.world.gen.biome;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.debug.WorldGenDebugContext;
import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.PosOutOfBoundsException;
import com.ultreon.craft.world.*;
import com.ultreon.craft.world.gen.*;
import com.ultreon.craft.world.gen.layer.TerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.List;

import static com.ultreon.craft.world.World.CHUNK_SIZE;

public class BiomeGenerator implements ServerDisposable {
    private final NoiseInstance biomeNoise;
    private final List<TerrainLayer> layers;
    private final List<WorldGenFeature> features;
    public static final boolean USE_DOMAIN_WARPING = true;
    @UnknownNullability
    public TreeGenerator treeGenerator;
    private final Biome biome;
    private final Carver carver;

    public BiomeGenerator(Biome biome, NoiseInstance noise, DomainWarping domainWarping, List<TerrainLayer> layers, List<WorldGenFeature> features) {
        this.biome = biome;
        this.biomeNoise = noise;
        this.carver = new Carver(domainWarping, noise);
        this.layers = layers;
        this.features = features;
    }

    public int processColumn(BuilderChunk chunk, int x, int z, Collection<ServerWorld.RecordedChange> recordedChanges) {
        int groundPos = this.carver.carve(chunk, x, z);
        for (int y = 0; y < CHUNK_SIZE; y++) {
            this.processCell(chunk, x, y, z, groundPos, recordedChanges);
        }

        return groundPos;
    }

    private void processCell(BuilderChunk chunk, int x, int y, int z, int groundPos, Collection<ServerWorld.RecordedChange> recordedChanges) {
        LightMap lightMap = chunk.getLightMap();
        this.paint(chunk.getWorld(), chunk, x, y, z, groundPos);

        BiomeGenerator.notifyChanges(chunk, x, y, z, recordedChanges);
        BiomeGenerator.lightUp(chunk, x, y, z, lightMap);
    }

    private static void lightUp(ChunkAccess world, int x, int y, int z, LightMap lightMap) {
        int highest = world.getHighest(x, z);
        y = World.localize(y);
        lightMap.setSunlight(x, y, z, y >= highest ? 15 : 7);
    }

    public void decorate(WorldAccess world, ChunkAccess chunk, int x, int z, int groundPos) {
        for (int y = 0; y < CHUNK_SIZE; y++) {
            for (var feature : this.features) {
                feature.handle(world, x + chunk.getOffset().x, y + chunk.getOffset().y, z + chunk.getOffset().z, groundPos);
            }
        }
    }

    private void paint(World world, Chunk chunk, int x, int y, int z, int groundPos) {
        if (chunk.get(x, y, z).isAir()) return;

        for (var layer : this.layers) {
            Block block = layer.handle(world, chunk, x + chunk.getOffset().x, y + chunk.getOffset().y, z + chunk.getOffset().z, groundPos);
            if (block != null) {
                if (!chunk.set(x, y, z, block)) {
                    UltracraftServer.get().crash(new PosOutOfBoundsException("Failed to set block " + block + " at worldspace coords: " + (x + chunk.getOffset().x) + ", " + (y + chunk.getOffset().y) + ", " + (z + chunk.getOffset().z) + " at chunk: " + chunk.getPos() + "."));
                }
                return;
            }
        }

        UltracraftServer.get().crash(new IllegalStateException("No terrain layer found for block " + chunk.get(x, y, z) + " at worldspace coords: " + (x + chunk.getOffset().x) + ", " + (y + chunk.getOffset().y) + ", " + (z + chunk.getOffset().z) + " at chunk: " + chunk.getPos() + "."));
    }

    private static void notifyChanges(BuilderChunk chunk, int x, int y, int z, Collection<ServerWorld.RecordedChange> recordedChanges) {
        for (ServerWorld.RecordedChange recordedChange : recordedChanges) {
            boolean isWithinChunkBounds = recordedChange.x() >= chunk.getOffset().x && recordedChange.x() < chunk.getOffset().x + World.CHUNK_SIZE
                    && recordedChange.y() >= chunk.getOffset().y && recordedChange.y() < chunk.getOffset().y + World.CHUNK_SIZE
                    && recordedChange.z() >= chunk.getOffset().z && recordedChange.z() < chunk.getOffset().z + World.CHUNK_SIZE;
            BlockPos localBlockPos = World.localizeBlock(recordedChange.x(), recordedChange.y(), recordedChange.z());
            if (isWithinChunkBounds && localBlockPos.x() == x && localBlockPos.y() == y && localBlockPos.z() == z) {
                chunk.set(World.localizeBlock(recordedChange.x(), recordedChange.y(), recordedChange.z()).vec(), recordedChange.block());
                chunk.getWorld().notifyChange(recordedChange);
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

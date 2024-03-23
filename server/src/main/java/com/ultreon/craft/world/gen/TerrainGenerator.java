package com.ultreon.craft.world.gen;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.debug.WorldGenDebugContext;
import com.ultreon.craft.util.MathHelper;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.BuilderChunk;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.biome.BiomeData;
import com.ultreon.craft.world.gen.biome.BiomeGenerator;
import com.ultreon.craft.world.gen.biome.BiomeIndex;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseConfig;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import de.articdive.jnoise.core.api.noisegen.NoiseGenerator;
import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex2DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex3DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex4DVariant;
import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.pipeline.JNoise;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ultreon.craft.world.World.CHUNK_SIZE;

public class TerrainGenerator {
    private final DomainWarping biomeDomain;
    private final DomainWarping layerDomain;
    private final NoiseConfig noiseConfig;
    @MonotonicNonNull
    private NoiseSource noise;
    private FloatList biomeNoise = new FloatArrayList();
    private final List<BiomeData> biomeGenData = new ArrayList<>();

    public TerrainGenerator(DomainWarping biomeDomain, DomainWarping layerDomain, NoiseConfig noiseConfig) {
        this.biomeDomain = biomeDomain;
        this.layerDomain = layerDomain;
        this.noiseConfig = noiseConfig;
    }

    public void create(ServerWorld world, long seed) {
        this.noise = JNoise.newBuilder()
                .fastSimplex(seed, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                .abs()
                .build();
    }

    @CanIgnoreReturnValue
    public BiomeData registerBiome(ServerWorld world, long seed, Biome biome, float temperatureStart, float temperatureEnd, boolean isOcean) {
        var generator = biome.create(world, seed);
        var biomeData = new BiomeData(temperatureStart, temperatureEnd, isOcean, generator);
        this.biomeGenData.add(biomeData);
        return biomeData;
    }

    @CanIgnoreReturnValue
    public BuilderChunk generate(BuilderChunk chunk, Collection<ServerWorld.RecordedChange> recordedChanges) {
//        this.buildBiomeCenters(chunk);

        RecordingChunk recordingChunk = new RecordingChunk(chunk);

        for (var x = 0; x < CHUNK_SIZE; x++) {
            for (var z = 0; z < CHUNK_SIZE; z++) {
                var index = this.findGenerator(chunk, new Vec3i(chunk.getOffset().x + x, 0, chunk.getOffset().z + z));
                chunk.setBiomeGenerator(x, z, index.biomeGenerator);
                chunk = index.biomeGenerator.processColumn(chunk, x, z, recordedChanges);
                chunk.getBiomeGenerator(x, z).generateTerrainFeatures(recordingChunk, x, z, chunk.getHighest(x, z));
            }
        }

        for (ServerWorld.RecordedChange change : recordingChunk.getRecordedChanges()) {
            if (WorldGenDebugContext.isActive()) {
                CommonConstants.LOGGER.info("Recorded change: " + change);
            }
            chunk.set(change.x(), change.y(), change.z(), change.block());
        }

        return chunk;
    }

    public void buildBiomeCenters(BuilderChunk chunk) {
        var biomeCenters = this.evalBiomeCenters(chunk.getOffset());

        for (var biomeCenter : biomeCenters) {
            var domainWarpingOffset = this.biomeDomain.generateDomainOffsetInt(biomeCenter.x, biomeCenter.z);
            biomeCenter.add(new Vec3i(domainWarpingOffset.x, 0, domainWarpingOffset.y));
        }
        this.biomeNoise = this.evalBiomeNoise(biomeCenters);
        chunk.setBiomeCenters(biomeCenters);
    }

    private List<Vec3i> evalBiomeCenters(Vec3i pos) {
        int len = CHUNK_SIZE;

        Vec3i origin = new Vec3i(Math.round((float) pos.x) / len, 0, Math.round((float) pos.z));
        var centers = new ListOrderedSet<Vec3i>();

        centers.add(origin);

        for (var dir : Neighbour8Direction.values()) {
            var offXZ = dir.vec();

            centers.add(new Vec3i(origin.x + offXZ.x * (len / 1), 0, origin.z + offXZ.y * (len / 1)));
            centers.add(new Vec3i(origin.x + offXZ.x * (len / 1), 0, origin.z + offXZ.y * 2 * (len / 1)));
            centers.add(new Vec3i(origin.x + offXZ.x * 2 * (len / 1), 0, origin.z + offXZ.y * (len / 1)));
            centers.add(new Vec3i(origin.x + offXZ.x * 2 * (len / 1), 0, origin.z + offXZ.y * 2 * (len / 1)));
        }

        return centers.asList();
    }

    private FloatList evalBiomeNoise(List<Vec3i> centers) {
        return centers.stream().map(center -> (float) this.noise.evaluateNoise(center.x, center.y)).collect(Collectors.toCollection(FloatArrayList::new));
    }

    private BiomeGenerator.Index findGenerator(BuilderChunk chunk, Vec3i offset) {
        return this.findGenerator(chunk, offset, true);
    }

    private BiomeGenerator.Index findGenerator(BuilderChunk chunk, Vec3i offset, boolean useDomainWarping) {
        if (useDomainWarping) {
            Vec2i domainOffset = MathHelper.round(this.biomeDomain.generateDomainOffset(offset.x, offset.z));
            offset.add(domainOffset.x, 0, domainOffset.y);
        }

        var localOffset = World.toLocalBlockPos(offset.x, offset.y, offset.z);
        var temp = this.noise.evaluateNoise(offset.x * this.noiseConfig.noiseZoom(), offset.z * this.noiseConfig.noiseZoom()) * 2.0f;
        chunk.getHighest(localOffset.x(), localOffset.z());
        BiomeGenerator biomeGen = this.biomeGenData.get(0).biomeGen();

        for (var data : this.biomeGenData) {
//            var currentlyOcean = height < World.SEA_LEVEL - 4;
            if (temp >= data.temperatureStartThreshold() && temp < data.temperatureEndThreshold() && !data.isOcean())
                biomeGen = data.biomeGen();
        }

        return new BiomeGenerator.Index(biomeGen);

    }

    private BiomeGenerator selectBiome(int index, float height) {
        var temp = this.biomeNoise.getFloat(index);
        for (var data : this.biomeGenData) {
            var currentlyOcean = height < World.SEA_LEVEL - 4;
            if (temp >= data.temperatureStartThreshold() && temp < data.temperatureEndThreshold() && currentlyOcean == data.isOcean())
                return data.biomeGen();
        }
        return this.biomeGenData.get(0).biomeGen();
    }

    private List<BiomeIndex> getBiomeIndexAt(BuilderChunk chunk, Vec3i offset) {
        offset.y = 0;
        return this.getClosestBiomeIndex(chunk, offset);
    }

    private List<BiomeIndex> getClosestBiomeIndex(BuilderChunk chunk, Vec3i gPos) {
        List<BiomeIndex> indices = new ArrayList<>();

        var centers = chunk.getBiomeCenters();
        var bound = centers.size();
        for (var index = 0; index < bound; index++) {
            var center = centers.get(index);
            indices.add(new BiomeIndex(index, center.dst(gPos.x, gPos.y, gPos.z)));
        }

        indices.sort(Comparator.comparingDouble(BiomeIndex::distance));
        return indices;
    }

    public DomainWarping getLayerDomain() {
        return this.layerDomain;
    }

    public void dispose() {
        this.biomeGenData.forEach(data -> data.biomeGen().dispose());
    }
}

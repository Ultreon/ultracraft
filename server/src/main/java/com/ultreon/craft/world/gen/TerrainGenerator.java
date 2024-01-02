package com.ultreon.craft.world.gen;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
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
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TerrainGenerator {
    private final DomainWarping biomeDomain;
    private final DomainWarping layerDomain;
    private final NoiseConfig noiseConfig;
    @MonotonicNonNull
    private NoiseInstance noise;
    private FloatList biomeNoise = new FloatArrayList();
    private final List<BiomeData> biomeGenData = new ArrayList<>();

    public TerrainGenerator(DomainWarping biomeDomain, DomainWarping layerDomain, NoiseConfig noiseConfig) {
        this.biomeDomain = biomeDomain;
        this.layerDomain = layerDomain;
        this.noiseConfig = noiseConfig;
    }

    public void create(ServerWorld world, long seed) {
        this.noise = this.noiseConfig.create(seed);
    }

    @CanIgnoreReturnValue
    public BiomeData registerBiome(ServerWorld world, long seed, Biome biome, float temperatureStart, float temperatureEnd) {
        var generator = biome.create(world, seed);
        var biomeData = new BiomeData(temperatureStart, temperatureEnd, generator);
        this.biomeGenData.add(biomeData);
        return biomeData;
    }

    @CanIgnoreReturnValue
    public BuilderChunk generate(BuilderChunk chunk, List<ServerWorld.RecordedChange> recordedChanges) {
        this.buildBiomeCenters(chunk);

        var index = this.findGenerator(chunk, chunk.getOffset(), false);
        chunk.setTreeData(index.biomeGenerator.createTreeData(chunk));

        for (var x = 0; x < chunk.size; x++) {
            for (var z = 0; z < chunk.size; z++) {
                index = this.findGenerator(chunk, new Vec3i(chunk.getOffset().x + x, 0, chunk.getOffset().z + z));
                chunk.setBiomeGenerator(x, z, index.biomeGenerator);
                chunk = index.biomeGenerator.processColumn(chunk, x, z, recordedChanges);
            }
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
        int len = World.CHUNK_SIZE;

        Vec3i origin = new Vec3i(Math.round((float) pos.x / len) * len, 0, Math.round((float) pos.z / len));
        var centers = new ListOrderedSet<Vec3i>();

        centers.add(origin);

        for (var dir : Neighbour8Direction.values()) {
            var offXZ = dir.vec();

            centers.add(new Vec3i(origin.x + offXZ.x * len, 0, origin.z + offXZ.y * len));
            centers.add(new Vec3i(origin.x + offXZ.x * len, 0, origin.z + offXZ.y * 2 * len));
            centers.add(new Vec3i(origin.x + offXZ.x * 2 * len, 0, origin.z + offXZ.y * len));
            centers.add(new Vec3i(origin.x + offXZ.x * 2 * len, 0, origin.z + offXZ.y * 2 * len));
        }

        return centers.asList();
    }

    private FloatList evalBiomeNoise(List<Vec3i> centers) {
        return centers.stream().map(center -> (float) this.noise.eval(center.x, center.y)).collect(Collectors.toCollection(FloatArrayList::new));
    }

    private BiomeGenerator.Index findGenerator(BuilderChunk chunk, Vec3i offset) {
        return this.findGenerator(chunk, offset, true);
    }

    private BiomeGenerator.Index findGenerator(BuilderChunk chunk, Vec3i offset, boolean useDomainWarping) {
        if (useDomainWarping) {
            Vec2i domainOffset = MathHelper.round(this.biomeDomain.generateDomainOffset(offset.x, offset.z));
            offset.add(domainOffset.x, 0, domainOffset.y);
        }

        var biomeIndices = this.getBiomeIndexAt(chunk, offset);

        var firstGenerator = this.selectBiome(biomeIndices.get(0).index());
        var secondGenerator = this.selectBiome(biomeIndices.get(1).index());

        var biomeCenters = chunk.getBiomeCenters();
        double distance = biomeCenters.get(biomeIndices.get(0).index()).dst(biomeCenters.get(biomeIndices.get(1).index()));
        double firstWeight = biomeIndices.get(0).distance() / distance;
        double secondWeight = 1 - firstWeight;
        int firstNoise = firstGenerator.getCarver().getSurfaceHeightNoise(offset.x, offset.z);
        int secondNoise = secondGenerator.getCarver().getSurfaceHeightNoise(offset.x, offset.z);
        return new BiomeGenerator.Index(firstGenerator, (int) Math.round(firstNoise * firstWeight + secondNoise * secondWeight));

    }

    private BiomeGenerator selectBiome(int index) {
        var temp = this.biomeNoise.getFloat(index);
        for (var data : this.biomeGenData) {
            if (temp >= data.temperatureStartThreshold() && temp < data.temperatureEndThreshold())
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

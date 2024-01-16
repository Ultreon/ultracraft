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

import static com.ultreon.craft.world.World.CHUNK_SIZE;

/**
 * Generates terrain for a {@link ServerWorld}.
 * 
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 * @see World
 */
public class TerrainGenerator {
    private final DomainWarping biomeDomain;
    private final DomainWarping layerDomain;
    private final NoiseConfig noiseConfig;
    @MonotonicNonNull
    private NoiseInstance noise;
    private FloatList biomeNoise = new FloatArrayList();
    private final List<BiomeData> biomeGenData = new ArrayList<>();

    /**
     * Creates a new terrain generator with the specified parameters.
     * 
     * @param biomeDomain the domain warping for the biomes
     * @param layerDomain the domain warping for the layers
     * @param noiseConfig the noise configuration for the terrain
     */
    public TerrainGenerator(DomainWarping biomeDomain, DomainWarping layerDomain, NoiseConfig noiseConfig) {
        this.biomeDomain = biomeDomain;
        this.layerDomain = layerDomain;
        this.noiseConfig = noiseConfig;
    }

    /**
     * Initializes the terrain generator to use for the specified world and seed.
     * 
     * @param world the world to initialize the terrain generator for
     * @param seed  the seed to use for the terrain generator
     */
    public void create(ServerWorld world, long seed) {
        this.noise = this.noiseConfig.create(seed);
    }

    /**
     * Register a new biome with the specified parameters.
     * 
     * @param world            the world to register the biome in
     * @param seed             the seed to use for the biome
     * @param biome            the biome to register
     * @param temperatureStart the start temperature of the biome
     * @param temperatureEnd   the end temperature of the biome
     * @param isOcean          whether the biome is an ocean
     * @return                 the biome data
     */
    @CanIgnoreReturnValue
    public BiomeData registerBiome(ServerWorld world, long seed, Biome biome, float temperatureStart, float temperatureEnd, boolean isOcean) {
        var generator = biome.create(world, seed);
        var biomeData = new BiomeData(temperatureStart, temperatureEnd, isOcean, generator);
        this.biomeGenData.add(biomeData);
        return biomeData;
    }

    /**
     * Fills in the data for the specified builder chunk.
     * 
     * @param chunk           the builder chunk to fill in
     * @param recordedChanges the recorded changes from other chunks to apply
     * @return                the filled in builder chunk
     */
    @CanIgnoreReturnValue
    public BuilderChunk generate(BuilderChunk chunk, List<ServerWorld.RecordedChange> recordedChanges) {
        for (var x = 0; x < CHUNK_SIZE; x++) {
            for (var z = 0; z < CHUNK_SIZE; z++) {
                var index = this.findGenerator(chunk, new Vec3i(chunk.getOffset().x + x, 0, chunk.getOffset().z + z));
                chunk.setBiomeGenerator(x, z, index.biomeGenerator);
                chunk = index.biomeGenerator.processColumn(chunk, x, z, recordedChanges);
            }
        }
        return chunk;
    }

    /**
     * Fills in the biome centers for the specified builder chunk.
     * 
     * @param chunk the builder chunk to fill in
     */
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

        Vec3i origin = new Vec3i(pos.x / len, 0, pos.z);
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

        var localOffset = World.toLocalBlockPos(offset.x, offset.y, offset.z);
        var temp = this.noise.eval(offset.x * this.noise.noiseZoom(), offset.z * this.noise.noiseZoom());
        var height = chunk.getHighest(localOffset.x(), localOffset.z());
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

    /**
     * Get the domain warping for the layers of this generator
     * 
     * @return the domain warping
     */
    public DomainWarping getLayerDomain() {
        return this.layerDomain;
    }

    /**
     * Cleans up everything used by this generator.
     * <p>Required to be called when the server shuts down. (Or when a local world is disposed)</p>
     */
    public void dispose() {
        this.biomeGenData.forEach(data -> data.biomeGen().dispose());
    }
}

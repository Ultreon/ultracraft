package com.ultreon.craft.world.gen;

import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.BiomeSelectionHelper;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.MyNoise;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TerrainGenerator {
    private ArrayList<Vec3i> biomeCenters = new ArrayList<>();
    private final DomainWarping biomeWarp;
    private final NoiseInstance noise;
    private DoubleList biomeNoises = new DoubleArrayList();
    private final List<Biome> biomes = new ArrayList<>();

    public TerrainGenerator(DomainWarping biomeWarp, NoiseInstance noise) {
        this.biomeWarp = biomeWarp;
        this.noise = noise;
    }

    public static Builder builder(DomainWarping biomeWarp, NoiseInstance noiseSettings) {
        return new Builder(biomeWarp, noiseSettings);
    }

    public Chunk generateChunkData(World world, Chunk chunk, long seed) {
        BiomeGeneratorSelection biomeSelection = this.selectBiomeGenerator(chunk.getOffset().cpy(), chunk, true);
        //TreeData treeData = biomeGenerator.GetTreeData(chunk, seed);
        chunk.treeData = biomeSelection.biomeGenerator.getTreeData(chunk, seed);

        for (int x = 0; x < chunk.size; x++) {
            for (int z = 0; z < chunk.size; z++) {
                biomeSelection = this.selectBiomeGenerator(new Vec3i(chunk.getOffset().x + x, 0, chunk.getOffset().z + z), chunk);
                chunk = biomeSelection.biomeGenerator.processColumn(world, chunk, x, z, seed, biomeSelection.terrainSurfaceNoise);
            }
        }
        return chunk;
    }

    private BiomeGeneratorSelection selectBiomeGenerator(Vec3i worldPosition, Chunk chunk) {
        return this.selectBiomeGenerator(worldPosition, chunk, true);
    }

    private BiomeGeneratorSelection selectBiomeGenerator(Vec3i worldPosition, Chunk chunk, boolean useDomainWarping) {
        if (useDomainWarping) {
            Vec2i domainOffset = this.biomeWarp.generateDomainOffset(worldPosition.x, worldPosition.z).i();
            worldPosition.add(domainOffset.x, 0, domainOffset.y);
        }

        List<BiomeSelectionHelper> selHelpers = this.getBiomeGeneratorSelectionHelpers(worldPosition);

        BiomeGenerator gen1 = this.selectBiome(selHelpers.get(0).index());
        BiomeGenerator gen2 = this.selectBiome(selHelpers.get(1).index());

        double distance = this.biomeCenters.get(selHelpers.get(0).index()).dst(this.biomeCenters.get(selHelpers.get(1).index()));
        double weight0 = selHelpers.get(0).distance() / distance;
        double weight1 = 1 - weight0;
        int terrainHeightNoise0 = gen1.getSurfaceHeightNoise(worldPosition.x, worldPosition.z, chunk.height);
        int terrainHeightNoise1 = gen2.getSurfaceHeightNoise(worldPosition.x, worldPosition.z, chunk.height);
        return new BiomeGeneratorSelection(gen1, (int) Math.round(terrainHeightNoise0 * weight0 + terrainHeightNoise1 * weight1));

    }

    private BiomeGenerator selectBiome(int index) {
        double temp = this.biomeNoises.getDouble(index);
        for (Biome data : this.biomes) {
            if (temp >= data.temperatureStartThreshold() && temp < data.temperatureEndThreshold())
                return data.biomeGen();
        }
        return this.biomes.get(0).biomeGen();
    }

    private List<BiomeSelectionHelper> getBiomeGeneratorSelectionHelpers(Vec3i position) {
        position.y = 0;
        return this.getClosestBiomeIndex(position);
    }

    private List<BiomeSelectionHelper> getClosestBiomeIndex(Vec3i position) {
        List<BiomeSelectionHelper> helpers = new ArrayList<>();

        for (int index = 0; index < this.biomeCenters.size(); index++) {
            Vec3i center = this.biomeCenters.get(index);
            helpers.add(new BiomeSelectionHelper(index, center.dst(position.x, position.y, position.z)));
        }

        helpers.sort(Comparator.comparingDouble(BiomeSelectionHelper::distance));
        return helpers;
    }

    public void generateBiomePoints(Vec3d player, int drawRange, int chunkSize) {
        this.biomeCenters = new ArrayList<>();
        this.biomeCenters = BiomeCenterFinder.calcBiomeCenters(player, drawRange, chunkSize);

        for (Vec3i biomeCenter : this.biomeCenters) {
            Vec2i domainWarpingOffset = this.biomeWarp.generateDomainOffsetInt(biomeCenter.x, biomeCenter.z);
            biomeCenter.add(new Vec3i(domainWarpingOffset.x, 0, domainWarpingOffset.y));
        }
        this.biomeNoises = this.calculateBiomeNoise(this.biomeCenters);
    }

    private DoubleList calculateBiomeNoise(List<Vec3i> biomeCenters) {
        return biomeCenters.stream().map(center -> MyNoise.octavePerlin(center.x, center.y, this.noise)).collect(DoubleArrayList::new, (doubles, key) -> doubles.add((double) key), DoubleList::addAll);
    }
    
    public void dispose() {
        this.biomeCenters.clear();
        this.biomeNoises.clear();
        this.biomeWarp.dispose();
        this.biomes.forEach(Biome::dispose);
        this.noise.dispose();
    }

    public static class Builder {
        private final TerrainGenerator terrainGen;

        public Builder(DomainWarping biomeWarp, NoiseInstance noiseSettings) {
            this.terrainGen = new TerrainGenerator(biomeWarp, noiseSettings);
        }

        public Builder biome(double tempStart, double tempEnd, BiomeGenerator generator) {
            this.terrainGen.biomes.add(new Biome((float) tempStart, (float) tempEnd, generator));
            return this;
        }

        public TerrainGenerator build() {
            return this.terrainGen;
        }
    }
}

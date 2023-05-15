package com.ultreon.craft.world.gen;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.util.Mth;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.BiomeSelectionHelper;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.MyNoise;
import com.ultreon.craft.world.gen.noise.NoiseSettings;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;

import java.util.ArrayList;
import java.util.List;

public class TerrainGenerator {
    private List<GridPoint3> biomeCenters = new ArrayList<>();
    private final DomainWarping biomeWarp;
    private final NoiseSettings biomeNoiseSettings;
    private FloatList biomeNoises = new FloatArrayList();
    private final List<Biome> biomes = new ArrayList<>();

    public TerrainGenerator(DomainWarping biomeWarp, NoiseSettings biomeNoiseSettings) {
        this.biomeWarp = biomeWarp;
        this.biomeNoiseSettings = biomeNoiseSettings;
    }

    public static Builder builder(DomainWarping biomeWarp, NoiseSettings noiseSettings) {
        return new Builder(biomeWarp, noiseSettings);
    }

    public Chunk generateChunkData(Chunk chunk, long seed) {
        BiomeGeneratorSelection biomeSelection = selectBiomeGenerator(chunk.offset, chunk, false);
        //TreeData treeData = biomeGenerator.GetTreeData(chunk, seed);
        chunk.treeData = biomeSelection.biomeGenerator.getTreeData(chunk, seed);

        for (int x = 0; x < chunk.size; x++) {
            for (int z = 0; z < chunk.size; z++) {
                biomeSelection = selectBiomeGenerator(new GridPoint3(chunk.offset.x + x, 0, chunk.offset.z + z), chunk);
                chunk = biomeSelection.biomeGenerator.processColumn(chunk, x, z, seed, biomeSelection.terrainSurfaceNoise);
            }
        }
        return chunk;
    }

    private BiomeGeneratorSelection selectBiomeGenerator(GridPoint3 worldPosition, Chunk chunk) {
        return selectBiomeGenerator(worldPosition, chunk, true);
    }

    private BiomeGeneratorSelection selectBiomeGenerator(GridPoint3 worldPosition, Chunk chunk, boolean useDomainWarping) {
        if (useDomainWarping) {
            GridPoint2 domainOffset = Mth.round(biomeWarp.generateDomainOffset(worldPosition.x, worldPosition.z));
            worldPosition.add(domainOffset.x, 0, domainOffset.y);
        }

        List<BiomeSelectionHelper> selHelpers = getBiomeGeneratorSelectionHelpers(worldPosition);

        BiomeGenerator gen1 = selectBiome(selHelpers.get(0).index());
        BiomeGenerator gen2 = selectBiome(selHelpers.get(1).index());

        float distance = biomeCenters.get(selHelpers.get(0).index()).dst(biomeCenters.get(selHelpers.get(1).index()));
        float weight0 = selHelpers.get(0).distance() / distance;
        float weight1 = 1 - weight0;
        int terrainHeightNoise0 = gen1.getSurfaceHeightNoise(worldPosition.x, worldPosition.z, chunk.height);
        int terrainHeightNoise1 = gen2.getSurfaceHeightNoise(worldPosition.x, worldPosition.z, chunk.height);
        return new BiomeGeneratorSelection(gen1, Math.round(terrainHeightNoise0 * weight0 + terrainHeightNoise1 * weight1));

    }

    private BiomeGenerator selectBiome(int index) {
        float temp = biomeNoises.getFloat(index);
        for (var data : biomes) {
            if (temp >= data.temperatureStartThreshold() && temp < data.temperatureEndThreshold())
                return data.biomeGen();
        }
        return biomes.get(0).biomeGen();
    }

    private List<BiomeSelectionHelper> getBiomeGeneratorSelectionHelpers(GridPoint3 position) {
        position.y = 0;
        return getClosestBiomeIndex(position);
    }

    private List<BiomeSelectionHelper> getClosestBiomeIndex(GridPoint3 position) {
        List<BiomeSelectionHelper> helpers = new ArrayList<>();

        for (int index = 0; index < biomeCenters.size(); index++) {
            GridPoint3 center = biomeCenters.get(index);
            helpers.add(new BiomeSelectionHelper(index, center.dst(position)));
        }

        helpers.sort((o1, o2) -> Float.compare(o1.distance(), o2.distance()));
        return helpers;
    }

    public void generateBiomePoints(Vector3 player, int drawRange, int chunkSize) {
        biomeCenters = new ArrayList<>();
        biomeCenters = BiomeCenterFinder.CalculatedBiomeCenters(player, drawRange, chunkSize);

        for (GridPoint3 biomeCenter : biomeCenters) {
            GridPoint2 domainWarpingOffset = biomeWarp.generateDomainOffsetInt(biomeCenter.x, biomeCenter.z);
            biomeCenter.add(new GridPoint3(domainWarpingOffset.x, 0, domainWarpingOffset.y));
        }
        biomeNoises = calculateBiomeNoise(biomeCenters);
    }

    private FloatList calculateBiomeNoise(List<GridPoint3> biomeCenters) {
        return biomeCenters.stream().map(center -> MyNoise.octavePerlin(center.x, center.y, biomeNoiseSettings)).collect(FloatArrayList::new, (floats, key) -> floats.add((float) key), FloatList::addAll);
    }

    public static class Builder {
        private final TerrainGenerator terrainGen;

        public Builder(DomainWarping biomeWarp, NoiseSettings noiseSettings) {
            this.terrainGen = new TerrainGenerator(biomeWarp, noiseSettings);
        }

        public Builder biome(float tempStart, float tempEnd, BiomeGenerator generator) {
            this.terrainGen.biomes.add(new Biome(tempStart, tempEnd, generator));
            return this;
        }

        public TerrainGenerator build() {
            return terrainGen;
        }
    }
}

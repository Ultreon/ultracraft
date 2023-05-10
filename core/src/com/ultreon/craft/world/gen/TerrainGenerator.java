package com.ultreon.craft.world.gen;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.util.Mth;
import com.ultreon.craft.world.BiomeData;
import com.ultreon.craft.world.BiomeSelectionHelper;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;

import java.util.ArrayList;
import java.util.List;

public class TerrainGenerator {
    private final DomainWarping biomeDomainWarping;
    private final List<Vector3> biomeCenters = new ArrayList<>();
    private final FloatList biomeNoise = new FloatArrayList();
    private List<BiomeData> biomeGenData = new ArrayList<>();

    public TerrainGenerator(DomainWarping biomeDomainWarping) {
        this.biomeDomainWarping = biomeDomainWarping;
    }

    public Chunk generateChunkData(Chunk chunk, long seed) {
        BiomeGeneratorSelection biomeSelection = selectBiomeGenerator(chunk.offset, chunk, false);
        //TreeData treeData = biomeGenerator.GetTreeData(chunk, seed);
        chunk.treeData = biomeSelection.biomeGenerator.getTreeData(chunk, seed);

        for (int x = 0; x < chunk.size; x++) {
            for (int z = 0; z < chunk.size; z++) {
                biomeSelection = selectBiomeGenerator(new Vector3((int) (chunk.offset.x + x), 0, (int) (chunk.offset.z + z)), chunk);
                chunk = biomeSelection.biomeGenerator.processColumn(chunk, x, z, seed, biomeSelection.terrainSurfaceNoise);
            }
        }
        return chunk;
    }

    private BiomeGeneratorSelection selectBiomeGenerator(Vector3 worldPosition, Chunk chunk) {
        return selectBiomeGenerator(worldPosition, chunk, true);
    }

    private BiomeGeneratorSelection selectBiomeGenerator(Vector3 worldPosition, Chunk chunk, boolean useDomainWarping) {
        if (useDomainWarping) {
            Vector2 domainOffset = Mth.round(biomeDomainWarping.GenerateDomainOffset((int) worldPosition.x, Math.round(worldPosition.z)));
            worldPosition.add(domainOffset.x, 0, domainOffset.y);
        }

        List<BiomeSelectionHelper> biomeSelectionHelpers = getBiomeGeneratorSelectionHelpers(worldPosition);

        BiomeGenerator generator_1 = selectBiome(biomeSelectionHelpers.get(0).index());
        BiomeGenerator generator_2 = selectBiome(biomeSelectionHelpers.get(1).index());

        float distance = biomeCenters.get(biomeSelectionHelpers.get(0).index()).dst(biomeCenters.get(biomeSelectionHelpers.get(1).index()));
        float weight_0 = biomeSelectionHelpers.get(0).distance() / distance;
        float weight_1 = 1 - weight_0;
        int terrainHeightNoise_0 = generator_1.getSurfaceHeightNoise(worldPosition.x, worldPosition.z, chunk.height);
        int terrainHeightNoise_1 = generator_2.getSurfaceHeightNoise(worldPosition.x, worldPosition.z, chunk.height);
        return new BiomeGeneratorSelection(generator_1, Math.round(terrainHeightNoise_0 * weight_0 + terrainHeightNoise_1 * weight_1));

    }

    private BiomeGenerator selectBiome(int index) {
        float temp = biomeNoise.getFloat(index);
        for (var data : biomeGenData) {
            if (temp >= data.temperatureStartThreshold() && temp < data.temperatureEndThreshold())
                return data.biomeGen();
        }
        return biomeGenData.get(0).biomeGen();
    }

    private List<BiomeSelectionHelper> getBiomeGeneratorSelectionHelpers(Vector3 position) {
        position.y = 0;
        return getClosestBiomeIndex(position);
    }

    private List<BiomeSelectionHelper> getClosestBiomeIndex(Vector3 position) {
        List<BiomeSelectionHelper> helpers = new ArrayList<>();

        for (int index = 0; index < biomeCenters.size(); index++) {
            Vector3 center = biomeCenters.get(index);
            helpers.add(new BiomeSelectionHelper(index, center.dst(position)));
        }

        helpers.sort((o1, o2) -> Float.compare(o1.distance(), o2.distance()));
        return helpers;
    }

}
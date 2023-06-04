package com.ultreon.craft.world.gen;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.GridPoint3;
import com.ultreon.craft.util.Mth;
import com.ultreon.craft.world.*;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;

import java.util.ArrayList;
import java.util.List;

public class TerrainGenerator {
    private final DomainWarping biomeDomainWarping;
    private final List<GridPoint3> biomeCenters = new ArrayList<>();
    private final FloatList biomeNoise = new FloatArrayList();
    private List<BiomeData> biomeGenData = new ArrayList<>();

    public TerrainGenerator(DomainWarping biomeDomainWarping) {
        this.biomeDomainWarping = biomeDomainWarping;
    }

    public RawChunk generateChunkData(World world, RawChunk chunk, long seed) {
        BiomeGeneratorSelection biomeSelection;

        for (int x = 0; x < chunk.size; x++) {
            for (int z = 0; z < chunk.size; z++) {
                biomeSelection = selectBiomeGenerator(new GridPoint3(chunk.offset.x + x, 0, chunk.offset.z + z), chunk);
                chunk = biomeSelection.biomeGenerator.processColumn(world, chunk, x, z, seed);
            }
        }
        return chunk;
    }

    private BiomeGeneratorSelection selectBiomeGenerator(GridPoint3 worldPosition, RawChunk chunk) {
        return selectBiomeGenerator(worldPosition, chunk, true);
    }

    private BiomeGeneratorSelection selectBiomeGenerator(GridPoint3 worldPosition, RawChunk chunk, boolean useDomainWarping) {
        if (useDomainWarping) {
            GridPoint2 domainOffset = Mth.round(biomeDomainWarping.generateDomainOffset(worldPosition.x, worldPosition.z));
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

}

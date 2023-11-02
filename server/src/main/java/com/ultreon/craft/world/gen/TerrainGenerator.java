package com.ultreon.craft.world.gen;

import com.ultreon.craft.util.MathHelper;
import com.ultreon.craft.world.BiomeData;
import com.ultreon.craft.world.BiomeSelectionHelper;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TerrainGenerator {
    private final DomainWarping biomeDomainWarping;
    private final List<Vec3i> biomeCenters = new ArrayList<>();
    private final FloatList biomeNoise = new FloatArrayList();
    private List<BiomeData> biomeGenData = new ArrayList<>();

    public TerrainGenerator(DomainWarping biomeDomainWarping) {
        this.biomeDomainWarping = biomeDomainWarping;
    }

    public Chunk generateChunkData(Chunk chunk, long seed) {
        BiomeGeneratorSelection biomeSelection = this.selectBiomeGenerator(chunk.getOffset(), false);
        //TreeData treeData = biomeGenerator.GetTreeData(chunk, seed);
        chunk.treeData = biomeSelection.biomeGenerator.getTreeData(chunk, seed);

        for (int x = 0; x < chunk.size; x++) {
            for (int z = 0; z < chunk.size; z++) {
                biomeSelection = this.selectBiomeGenerator(new Vec3i(chunk.getOffset().x + x, 0, chunk.getOffset().z + z));
                chunk = biomeSelection.biomeGenerator.processColumn(chunk, x, z);
            }
        }
        return chunk;
    }

    private BiomeGeneratorSelection selectBiomeGenerator(Vec3i worldPosition) {
        return this.selectBiomeGenerator(worldPosition, true);
    }

    private BiomeGeneratorSelection selectBiomeGenerator(Vec3i worldPosition, boolean useDomainWarping) {
        if (useDomainWarping) {
            Vec2i domainOffset = MathHelper.round(this.biomeDomainWarping.generateDomainOffset(worldPosition.x, worldPosition.z));
            worldPosition.add(domainOffset.x, 0, domainOffset.y);
        }

        List<BiomeSelectionHelper> biomeSelectionHelpers = this.getBiomeGeneratorSelectionHelpers(worldPosition);

        BiomeGenerator generator_1 = this.selectBiome(biomeSelectionHelpers.get(0).index());
        BiomeGenerator generator_2 = this.selectBiome(biomeSelectionHelpers.get(1).index());

        double distance = this.biomeCenters.get(biomeSelectionHelpers.get(0).index()).dst(this.biomeCenters.get(biomeSelectionHelpers.get(1).index()));
        double weight_0 = biomeSelectionHelpers.get(0).distance() / distance;
        double weight_1 = 1 - weight_0;
        int terrainHeightNoise_0 = generator_1.getSurfaceHeightNoise(worldPosition.x, worldPosition.z);
        int terrainHeightNoise_1 = generator_2.getSurfaceHeightNoise(worldPosition.x, worldPosition.z);
        return new BiomeGeneratorSelection(generator_1, (int) Math.round(terrainHeightNoise_0 * weight_0 + terrainHeightNoise_1 * weight_1));

    }

    private BiomeGenerator selectBiome(int index) {
        float temp = this.biomeNoise.getFloat(index);
        for (BiomeData data : this.biomeGenData) {
            if (temp >= data.temperatureStartThreshold() && temp < data.temperatureEndThreshold())
                return data.biomeGen();
        }
        return this.biomeGenData.get(0).biomeGen();
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

}

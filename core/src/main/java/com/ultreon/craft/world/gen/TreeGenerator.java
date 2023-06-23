package com.ultreon.craft.world.gen;

import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.craft.world.gen.noise.NoiseSettings;
import com.ultreon.craft.world.gen.trees.DataProcessing;

public class TreeGenerator {
    public NoiseSettings treeNoiseSettings;
    public DomainWarping domainWrapping;

    public TreeData generateTreeData(Chunk chunkData, long seed) {
        NoiseInstance noise = this.treeNoiseSettings.create(seed);
        TreeData treeData = new TreeData();
        float[][] noiseData = this.generateTreeNoise(chunkData, noise);
        treeData.treePositions = DataProcessing.findLocalMaxima(noiseData, chunkData.getOffset().x, chunkData.getOffset().z);

        return treeData;
    }

    private float[][] generateTreeNoise(Chunk chunkData, NoiseInstance noise) {
        float[][] noiseMax = new float[chunkData.size][chunkData.size];
        int xMax = chunkData.getOffset().x + chunkData.size;
        int xMin = chunkData.getOffset().x;
        int zMax = chunkData.getOffset().z + chunkData.size;
        int zMin = chunkData.getOffset().z;
        int xIndex = 0, zIndex = 0;

        for (int x = xMin; x < xMax; x++) {
            for (int z = zMin; z < zMax; z++) {
                noiseMax[xIndex][zIndex] = this.domainWrapping.generateDomainNoise(x, z, noise);
                zIndex++;
            }

            xIndex++;
            zIndex = 0;
        }

        return noiseMax;
    }
}

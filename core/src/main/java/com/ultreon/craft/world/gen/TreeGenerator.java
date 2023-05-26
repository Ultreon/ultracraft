package com.ultreon.craft.world.gen;

import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseSettings;
import com.ultreon.craft.world.gen.trees.DataProcessing;

public class TreeGenerator {
    public NoiseSettings treeNoiseSettings;
    public DomainWarping domainWrapping;

    public TreeData generateTreeData(Chunk chunkData, long seed)
    {
        treeNoiseSettings.setSeed(seed);
        TreeData treeData = new TreeData();
        float[][] noiseData = generateTreeNoise(chunkData, treeNoiseSettings);
        treeData.treePositions = DataProcessing.findLocalMaxima(noiseData, (int)chunkData.offset.x, (int)chunkData.offset.z);

        return treeData;
    }

    private float[][] generateTreeNoise(Chunk chunkData, NoiseSettings treeNoiseSettings)
    {
        float[][] noiseMax = new float[chunkData.size][chunkData.size];
        int xMax = (int) (chunkData.offset.x + chunkData.size);
        int xMin = (int) chunkData.offset.x;
        int zMax = (int) (chunkData.offset.z + chunkData.size);
        int zMin = (int) chunkData.offset.z;
        int xIndex = 0, zIndex = 0;

        for (int x = xMin; x < xMax; x++)
        {
            for (int z = zMin; z < zMax; z++)
            {
                noiseMax[xIndex][zIndex] = domainWrapping.generateDomainNoise(x, z, treeNoiseSettings);
                zIndex++;
            }

            xIndex++;
            zIndex = 0;
        }

        return noiseMax;
    }
}

package com.ultreon.craft.world.gen;

import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.craft.world.gen.noise.NoiseSettings;
import com.ultreon.craft.world.gen.trees.DataProcessing;
import org.jetbrains.annotations.UnknownNullability;

public class TreeGenerator {
    private final NoiseSettings noiseSettings;
    private final DomainWarping warping;

    public TreeGenerator(long worldSeed, NoiseSettings noiseSettings, DomainWarping warping) {
        this.noiseSettings = noiseSettings.subSeed(worldSeed);
        this.warping = warping;
    }

    public TreeData generateTreeData(Chunk chunkData, long seed) {
        TreeData treeData = new TreeData();
        float[][] noiseData = generateTreeNoise(chunkData, noiseSettings);
        treeData.treePositions = DataProcessing.findLocalMaxima(noiseData, chunkData.offset.x, chunkData.offset.z);

        return treeData;
    }

    private float[][] generateTreeNoise(Chunk chunkData, NoiseInstance noise) {
        float[][] noiseMax = new float[chunkData.size][chunkData.size];
        int xMax = chunkData.getOffset().x + chunkData.size;
        int xMin = chunkData.getOffset().x;
        int zMax = chunkData.getOffset().z + chunkData.size;
        int zMin = chunkData.getOffset().z;
        int xIndex = 0, zIndex = 0;

        for (int x = xMin; x < xMax; x++)
        {
            for (int z = zMin; z < zMax; z++)
            {
                noiseMax[xIndex][zIndex] = warping.generateDomainNoise(x, z, treeNoiseSettings);
                zIndex++;
            }

            xIndex++;
            zIndex = 0;
        }

        return noiseMax;
    }
}

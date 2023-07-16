package com.ultreon.craft.world.gen;

import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.craft.world.gen.noise.NoiseSettings;
import com.ultreon.craft.world.gen.trees.DataProcessing;

public class TreeGenerator {
    private final NoiseInstance noise;
    private final DomainWarping warping;

    public TreeGenerator(long worldSeed, NoiseSettings noise, DomainWarping warping) {
        this.noise = noise.create(worldSeed);
        this.warping = warping;
    }

    public TreeData generateTreeData(Chunk chunkData, long seed) {
        TreeData treeData = new TreeData();
        double[][] noiseData = this.generateTreeNoise(chunkData, this.noise);
        treeData.treePositions = DataProcessing.findLocalMaxima(noiseData, chunkData.getOffset().x, chunkData.getOffset().z);

        return treeData;
    }

    private double[][] generateTreeNoise(Chunk chunkData, NoiseInstance noise) {
        double[][] noiseMax = new double[chunkData.size][chunkData.size];
        int xMax = chunkData.getOffset().x + chunkData.size;
        int xMin = chunkData.getOffset().x;
        int zMax = chunkData.getOffset().z + chunkData.size;
        int zMin = chunkData.getOffset().z;
        int xIndex = 0, zIndex = 0;

        for (int x = xMin; x < xMax; x++)
        {
            for (int z = zMin; z < zMax; z++)
            {
                noiseMax[xIndex][zIndex] = this.warping.generateDomainNoise(x, z, noise);
                zIndex++;
            }

            xIndex++;
            zIndex = 0;
        }

        return noiseMax;
    }

    public void dispose() {
        this.noise.dispose();
    }
}

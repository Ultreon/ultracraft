package com.ultreon.craft.world.gen.layer;

import com.badlogic.gdx.math.GridPoint3;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.RawChunk;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseSettings;

public class StonePatchTerrainLayer extends TerrainLayer {
    public float stoneThreshold = 0.5f;
    private final NoiseSettings noiseSettings;

    public DomainWarping domainWarping;

    public StonePatchTerrainLayer(NoiseSettings noiseSettings, DomainWarping domainWarping) {
        this.noiseSettings = noiseSettings;
        this.domainWarping = domainWarping;
    }

    @Override
    public boolean handle(RawChunk chunk, int x, int y, int z, int height, long seed) {
        if (chunk.offset.y > height)
            return false;

        noiseSettings.setSeed(seed);
        //float stoneNoise = MyNoise.OctavePerlin(chunkData.worldPosition.x + x, chunkData.worldPosition.z + z, stoneNoiseSettings);
        float stoneNoise = domainWarping.generateDomainNoise(chunk.offset.x + x, chunk.offset.z + z, noiseSettings);

        int endPosition = height;
        if (chunk.offset.y < 0) {
            endPosition = chunk.offset.y + chunk.height;
        }

        if (stoneNoise > stoneThreshold) {
            for (int i = chunk.offset.y; i <= endPosition; i++) {
                GridPoint3 pos = new GridPoint3(x, i, z);
                try {
                    chunk.set(pos, Blocks.STONE);
                } catch (Exception e) {
//                    System.out.println("pos = " + pos);
                    throw new RuntimeException("Execution error at " + pos, e);
                }
            }
            return true;
        }
        return false;
    }
}

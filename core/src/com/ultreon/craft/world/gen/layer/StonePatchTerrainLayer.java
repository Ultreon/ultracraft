package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.debug.Debugger;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.Chunk;
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
    public boolean handle(Chunk chunk, int x, int y, int z, int height, long seed) {
        Debugger.layersTriggered.add(this);
        if (chunk.offset.y > height)
            return false;

        noiseSettings.seed = seed;
        //float stoneNoise = MyNoise.OctavePerlin(chunkData.worldPosition.x + x, chunkData.worldPosition.z + z, stoneNoiseSettings);
        float stoneNoise = domainWarping.generateDomainNoise((int) (chunk.offset.x + x), (int) (chunk.offset.z + z), noiseSettings);

        int endPosition = height;
        if (chunk.offset.y < 0) {
            endPosition = (int) (chunk.offset.y + chunk.height);
        }

        if (stoneNoise > stoneThreshold) {
            for (int i = (int) chunk.offset.y; i <= endPosition; i++) {
                BlockPos pos = new BlockPos(x, i, z);
                try {
                    chunk.set(pos, Blocks.STONE.get());
                } catch (Exception e) {
//                    System.out.println("pos = " + pos);
                    throw new RuntimeException("Execution error at " + pos, e);
                }
            }
            Debugger.layersHandled.add(this);
            return true;
        }
        return false;
    }
}

package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.util.Vec2i;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseSettings;

public class StonePatchTerrainLayer extends TerrainLayer {
    public float stoneThreshold = 0.5f;
    private final NoiseSettings stoneNoiseSettings;

    public DomainWarping domainWarping;

    public StonePatchTerrainLayer(NoiseSettings stoneNoiseSettings, DomainWarping domainWarping) {
        this.stoneNoiseSettings = stoneNoiseSettings;
        this.domainWarping = domainWarping;
    }

    @Override
    public boolean handle(Chunk chunk, int x, int y, int z, int height, Vec2i mapSeed) {
        if (chunk.offset.y > height)
            return false;

        stoneNoiseSettings.worldOffset = mapSeed;
        //float stoneNoise = MyNoise.OctavePerlin(chunkData.worldPosition.x + x, chunkData.worldPosition.z + z, stoneNoiseSettings);
        float stoneNoise = domainWarping.generateDomainNoise((int) (chunk.offset.x + x), (int) (chunk.offset.z + z), stoneNoiseSettings);

        int endPosition = height;
        if (chunk.offset.y < 0) {
            endPosition = (int) (chunk.offset.y + chunk.height);
        }


        if (stoneNoise > stoneThreshold) {
            for (int i = (int) chunk.offset.y; i <= endPosition; i++) {
                BlockPos pos = new BlockPos(x, i, z);
                chunk.set(pos, Blocks.STONE.get());
            }
            return true;
        }
        return false;
    }
}

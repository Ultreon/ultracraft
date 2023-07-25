package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.libs.commons.v0.vector.Vec3i;

public class StonePatchTerrainLayer extends TerrainLayer {
    private final NoiseInstance noise;
    public float stoneThreshold = 0.5f;

    public DomainWarping domainWarping;

    public StonePatchTerrainLayer(NoiseInstance noise, DomainWarping domainWarping) {
        this.noise = noise;
        this.domainWarping = domainWarping;
    }

    @Override
    public boolean handle(World world, Chunk chunk, int x, int y, int z, int height, long seed) {
        if (chunk.getOffset().y > height)
            return false;

        //float stoneNoise = MyNoise.OctavePerlin(chunkData.worldPosition.x + x, chunkData.worldPosition.z + z, stoneNoiseSettings);
        float stoneNoise = this.domainWarping.generateDomainNoise(chunk.getOffset().x + x, chunk.getOffset().z + z, this.noise);

        int endPosition = height;
        if (chunk.getOffset().y < 0) {
            endPosition = chunk.getOffset().y + chunk.height;
        }

        if (stoneNoise > this.stoneThreshold) {
            for (int i = chunk.getOffset().y; i <= endPosition; i++) {
                Vec3i pos = new Vec3i(x, i, z);
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

    @Override
    public void dispose() {
        this.noise.dispose();
        this.domainWarping.dispose();
    }
}

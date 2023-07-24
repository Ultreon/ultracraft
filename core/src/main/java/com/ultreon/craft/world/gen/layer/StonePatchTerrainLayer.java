package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.libs.commons.v0.vector.Vec3i;


public class StonePatchTerrainLayer extends TerrainLayer {
    private final float stoneThreshold;
    private final NoiseInstance noiseSettings;

    public DomainWarping domainWarping;

    public StonePatchTerrainLayer(NoiseInstance noiseSettings, DomainWarping domainWarping) {
        this(0.5F, noiseSettings, domainWarping);
    }

    public StonePatchTerrainLayer(float stoneThreshold, NoiseInstance noiseSettings, DomainWarping domainWarping) {
        this.stoneThreshold = stoneThreshold;
        this.noiseSettings = noiseSettings;
        this.domainWarping = domainWarping;
    }

    @Override
    public boolean handle(World world, Chunk chunk, int x, int y, int z, int height) {
        if (chunk.getOffset().y > height)
            return false;

        double stoneNoise = this.domainWarping.generateDomainNoise(chunk.getOffset().x + x, chunk.getOffset().z + z, this.noiseSettings);

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
}

package com.ultreon.craft.world.gen.layer;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.debug.Debugger;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseSettings;
import com.ultreon.libs.commons.v0.vector.Vec3i;


public class StonePatchTerrainLayer extends TerrainLayer {
    private float stoneThreshold;
    private final NoiseSettings noiseSettings;

    public DomainWarping domainWarping;

    public StonePatchTerrainLayer(NoiseSettings noiseSettings, DomainWarping domainWarping) {
        this(0.5F, noiseSettings, domainWarping);
    }

    public StonePatchTerrainLayer(float stoneThreshold, NoiseSettings noiseSettings, DomainWarping domainWarping) {
        this.stoneThreshold = stoneThreshold;
        this.noiseSettings = noiseSettings;
        this.domainWarping = domainWarping;
    }

    @Override
    public boolean handle(Chunk chunk, int x, int y, int z, int height, long seed) {
        if (chunk.offset.y > height)
            return false;

        float stoneNoise = domainWarping.generateDomainNoise(chunk.offset.x + x, chunk.offset.z + z, noiseSettings);

        int endPosition = height;
        if (chunk.offset.y < 0) {
            endPosition = chunk.offset.y + chunk.height;
        }

        if (stoneNoise > stoneThreshold) {
            for (int i = chunk.offset.y; i <= endPosition; i++) {
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

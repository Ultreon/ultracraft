package com.ultreon.craft.world.gen;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.BuilderChunk;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.biome.BiomeGenerator;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.craft.world.gen.noise.NoiseUtils;

import static com.ultreon.craft.world.World.CHUNK_SIZE;

public class Carver {
    private final DomainWarping domainWarping;
    private final NoiseInstance biomeNoise;
    private final CaveNoiseGenerator caveNoise;

    public Carver(DomainWarping domainWarping, NoiseInstance biomeNoise) {
        this.domainWarping = domainWarping;
        this.biomeNoise = biomeNoise;

        this.caveNoise = new CaveNoiseGenerator(this.biomeNoise.seed());
    }

    @CanIgnoreReturnValue
    public int carve(BuilderChunk chunk, int x, int z) {
        int groundPos = this.getSurfaceHeightNoise(chunk.getOffset().x + x, chunk.getOffset().z + z);
        for (int y = 0; y < CHUNK_SIZE; y++) {
            if (chunk.getOffset().y + y <= groundPos) {
                double noise = this.caveNoise.evaluateNoise((chunk.getOffset().x + x) / 16f, (chunk.getOffset().y + y) / 16f, (chunk.getOffset().z + z) / 16f);
                chunk.set(x, y, z, noise == 1.0 ? Blocks.CAVE_AIR : Blocks.STONE);
            } else {
                chunk.set(x, y, z, Blocks.AIR);
            }
        }

        return groundPos >= chunk.getOffset().y + World.CHUNK_SIZE ? groundPos : chunk.getOffset().y + chunk.getHighestBlock(x, z);
    }

    public int getSurfaceHeightNoise(float x, float z) {
        double height;

        if (BiomeGenerator.USE_DOMAIN_WARPING)
            height = this.domainWarping.generateDomainNoise((int) x, (int) z, this.biomeNoise);
        else
            height = NoiseUtils.octavePerlin(x, z, this.biomeNoise);

        return (int) Math.ceil(Math.max(height, 1));
    }
}

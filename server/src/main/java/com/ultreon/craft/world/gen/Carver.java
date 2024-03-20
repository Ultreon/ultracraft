package com.ultreon.craft.world.gen;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.BuilderChunk;
import com.ultreon.craft.world.gen.biome.BiomeGenerator;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseInstance;
import com.ultreon.craft.world.gen.noise.NoiseUtils;

import static com.ultreon.craft.world.World.CHUNK_HEIGHT;

public class Carver {
    private final DomainWarping domainWarping;
    private final NoiseInstance biomeNoise;
    private final CaveNoiseGenerator caveNoise;

    public Carver(DomainWarping domainWarping, NoiseInstance biomeNoise) {
        this.domainWarping = domainWarping;
        this.biomeNoise = biomeNoise;

        this.caveNoise = new CaveNoiseGenerator(this.biomeNoise.seed());
    }

    public int carve(BuilderChunk chunk, int x, int z) {
        int groundPos = this.getSurfaceHeightNoise(chunk.getOffset().x + x, chunk.getOffset().z + z);
        for (int y = chunk.getOffset().y + 1; y < chunk.getOffset().y + CHUNK_HEIGHT; y++) {
            if (y <= groundPos) {
                double noise = this.caveNoise.evaluateNoise((chunk.getOffset().x + x) / 16f, y / 16f, (chunk.getOffset().z + z) / 16f);
                chunk.set(x, y, z, noise == 1.0 ? Blocks.CAVE_AIR.createMeta() : Blocks.STONE.createMeta());
            } else {
                chunk.set(x, y, z, Blocks.AIR.createMeta());
            }
        }

        return chunk.getHighest(x, z);
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

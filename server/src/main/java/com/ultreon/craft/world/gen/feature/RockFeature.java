package com.ultreon.craft.world.gen.feature;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.ChunkAccess;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.WorldGenFeature;
import com.ultreon.craft.world.gen.noise.NoiseConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class RockFeature extends WorldGenFeature {
    private final NoiseConfig noiseConfig;
    private final Block material;
    private final float threshold;
    private final Random random = new Random();

    public RockFeature(NoiseConfig trees, Block material, float threshold) {
        super();

        this.noiseConfig = trees;
        this.material = material;
        this.threshold = threshold;
    }

    @Override
    public boolean handle(@NotNull World world, @NotNull ChunkAccess chunk, int x, int z, int height) {
        if (this.noiseConfig == null) return false;

        height = chunk.getHighest(x, z);

        int posSeed = (x + chunk.getOffset().x) << 16 | (z + chunk.getOffset().z) & 0xFFFF;
        long seed = (world.getSeed() ^ this.noiseConfig.seed() << 32) ^ posSeed;
        this.random.setSeed(seed);
        this.random.setSeed(this.random.nextLong());

        if (this.random.nextFloat() < this.threshold) {
            for (int xOffset = -1; xOffset < 1; xOffset++) {
                for (int zOffset = -1; zOffset < 1; zOffset++) {
                    for (int y = height; y <= height + 1; y++) {
                        chunk.set(x + xOffset, y, z + zOffset, this.material.createMeta());
                    }
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public void create(@NotNull ServerWorld world) {

    }
}

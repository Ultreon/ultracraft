package com.ultreon.craft.world.gen.feature;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.debug.WorldGenDebugContext;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.gen.WorldAccess;
import com.ultreon.craft.world.gen.WorldGenFeature;
import com.ultreon.craft.world.gen.noise.NoiseConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class CactiFeature extends WorldGenFeature {
    private final NoiseConfig noiseConfig;
    private final Block block;
    private final float threshold;
    private final Random random = new Random();
    private final int minTrunkHeight;
    private final int maxTrunkHeight;

    public CactiFeature(NoiseConfig trees, Block block, float threshold, int minTrunkHeight, int maxTrunkHeight) {
        super();

        this.noiseConfig = trees;
        this.block = block;
        this.threshold = threshold;
        this.minTrunkHeight = minTrunkHeight;
        this.maxTrunkHeight = maxTrunkHeight;
    }

    @Override
    public boolean handle(@NotNull WorldAccess world, int x, int y, int z, int height) {
        if (y != height) return false;
        if (this.noiseConfig == null) return false;

        int posSeed = x << 16 | z & 0xFFFF;
        long seed = (world.getSeed() ^ this.noiseConfig.seed() << 32) ^ posSeed;
        this.random.setSeed(seed);
        this.random.setSeed(this.random.nextLong());

        if (this.random.nextFloat() < this.threshold) {
            if (WorldGenDebugContext.isActive()) {
                System.out.println("[Start " + Thread.currentThread().threadId() + "] TreeFeature: " + x + ", " + z + ", " + y);
            }

            var trunkHeight = this.random.nextInt(this.minTrunkHeight, this.maxTrunkHeight);

            // Check if there is enough space
            for (int yOffset = 0; yOffset < trunkHeight; yOffset++) {
                for (int xOffset = -1; xOffset <= 1; xOffset++) {
                    for (int zOffset = -1; zOffset <= 1; zOffset++) {
                        if (!world.get(x + xOffset, y + yOffset, z + zOffset).isAir()) {
                            return false;
                        }
                    }
                }
            }

            for (int yi = y; yi < y + trunkHeight; yi++) {
                world.set(x, yi, z, this.block);
            }
            return true;
        }

        return false;
    }

    @Override
    public void create(@NotNull ServerWorld world) {

    }
}

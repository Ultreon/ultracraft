package com.ultreon.craft.world.gen.feature;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.debug.WorldGenDebugContext;
import com.ultreon.craft.world.ChunkAccess;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.WorldGenFeature;
import com.ultreon.craft.world.gen.noise.NoiseConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static com.ultreon.craft.world.World.CHUNK_HEIGHT;

public class TreeFeature extends WorldGenFeature {
    private final NoiseConfig noiseConfig;
    private final Block trunk;
    private final Block leaves;
    private final float threshold;
    private final Random random = new Random();
    private final int minTrunkHeight;
    private final int maxTrunkHeight;

    public TreeFeature(NoiseConfig trees, Block trunk, Block leaves, float threshold, int minTrunkHeight, int maxTrunkHeight) {
        super();

        this.noiseConfig = trees;
        this.trunk = trunk;
        this.leaves = leaves;
        this.threshold = threshold;
        this.minTrunkHeight = minTrunkHeight;
        this.maxTrunkHeight = maxTrunkHeight;
    }

    @Override
    public boolean handle(@NotNull World world, @NotNull ChunkAccess chunk, int x, int z, int height) {
        if (this.noiseConfig == null) return false;

        int posSeed = (x + chunk.getOffset().x) << 16 | (z + chunk.getOffset().z) & 0xFFFF;
        long seed = (world.getSeed() ^ this.noiseConfig.seed() << 32) ^ posSeed;
        this.random.setSeed(seed);
        this.random.setSeed(this.random.nextLong());

        if (this.random.nextFloat() < this.threshold) {
            if (WorldGenDebugContext.isActive()) {
                System.out.println("[Start " + Thread.currentThread().threadId() + "] TreeFeature: " + x + ", " + z + ", " + height);
            }

            var trunkHeight = this.random.nextInt(this.minTrunkHeight, this.maxTrunkHeight);
            if (trunkHeight + height + 1 > CHUNK_HEIGHT) {
                if (WorldGenDebugContext.isActive()) {
                    System.out.println("[End " + Thread.currentThread().threadId() + "] TreeFeature: " + x + ", " + z + ", " + height);
                }
                return false;
            }

            // Check if there is enough space
            for (int y = height; y < height + trunkHeight; y++) {
                for (int xOffset = -1; xOffset <= 1; xOffset++) {
                    for (int zOffset = -1; zOffset <= 1; zOffset++) {
                        if (!chunk.get(x + xOffset, y, z + zOffset).isAir()){
                            if (WorldGenDebugContext.isActive()) {
                                System.out.println("[End " + Thread.currentThread().threadId() + "] TreeFeature: " + x + ", " + z + ", " + height + " - Not enough space");
                            }
                            return false;
                        }
                    }
                }
            }


            for (int y = height; y < height + trunkHeight; y++) {
                chunk.set(x, y, z, this.trunk.createMeta());
            }

            chunk.set(x, height - 1, z, Blocks.DIRT.createMeta());
            for (int xOffset = -1; xOffset <= 1; xOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    for (int y = height + trunkHeight - 1; y <= height + trunkHeight + 1; y++) {
                        chunk.set(x + xOffset, y, z + zOffset, this.leaves.createMeta());

                        if (WorldGenDebugContext.isActive()) {
                            System.out.println("[End " + Thread.currentThread().threadId() + "] TreeFeature: " + x + ", " + z + ", " + height + " - Setting leaf at " + (x + xOffset) + ", " + y + ", " + (z + zOffset));
                        }
                    }
                }
            }

            if (WorldGenDebugContext.isActive()) {
                System.out.println("[End " + Thread.currentThread().threadId() + "] TreeFeature: " + x + ", " + z + ", " + height + " - Success");
            }
            return true;
        }

        return false;
    }

    @Override
    public void create(@NotNull ServerWorld world) {

    }
}

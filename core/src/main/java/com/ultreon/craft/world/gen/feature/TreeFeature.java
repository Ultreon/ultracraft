package com.ultreon.craft.world.gen.feature;

import com.google.common.collect.Range;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.RawChunk;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.GenerationStage;

import java.util.Random;

public class TreeFeature extends Feature {
    private final Range<Integer> trunkHeight;

    public TreeFeature(GenerationStage stage, Range<Integer> trunkHeight) {
        super(stage);
        this.trunkHeight = trunkHeight;
    }

    @Override
    public void generate(World world, RawChunk chunk, int x, int y, int z, Random random) {
        chunk.setFast(x, y, z, Blocks.DIRT);

        int trunkHeight = random.nextInt(this.trunkHeight.lowerEndpoint(), this.trunkHeight.upperEndpoint());
        for (int ty = 0; ty < trunkHeight; ty++) {
            this.set(chunk, x, y + ty + 1, z, Blocks.LOG);
        }

        for (int lx = x - 2; lx <= x + 2; lx++) {
            for (int lz = z - 2; lz <= z + 2; lz++) {
                for (int ly = trunkHeight - 1; ly <= trunkHeight; ly++) {
                    if (lx == x && lz == z) continue;

                    this.set(chunk, lx, y + ly, lz, Blocks.LEAVES);
                }
            }
        }

        for (int ly = y + trunkHeight + 1; ly <= y + trunkHeight + 2; ly++) {
            this.set(chunk, x - 1, ly, z, Blocks.LEAVES);
            this.set(chunk, x + 1, ly, z, Blocks.LEAVES);
            this.set(chunk, x, ly, z - 1, Blocks.LEAVES);
            this.set(chunk, x, ly, z + 1, Blocks.LEAVES);
            this.set(chunk, x, ly, z, Blocks.LEAVES);
        }

        if (random.nextBoolean()) this.set(chunk, x - 1, y + trunkHeight + 1, z - 1, Blocks.LEAVES);
        if (random.nextBoolean()) this.set(chunk, x - 1, y + trunkHeight + 1, z + 1, Blocks.LEAVES);
        if (random.nextBoolean()) this.set(chunk, x + 1, y + trunkHeight + 1, z - 1, Blocks.LEAVES);
        if (random.nextBoolean()) this.set(chunk, x + 1, y + trunkHeight + 1, z + 1, Blocks.LEAVES);
    }

    @Override
    public boolean canGenerate(World world, RawChunk chunk, int x, int y, int z, Random random) {
        int height = chunk.getHeight(x, z);
        if (y != height) return false;

        if (this.isAllAir(chunk, x - 3, y + (this.trunkHeight.lowerEndpoint() - 2), z - 3, x + 3, y + this.trunkHeight.upperEndpoint(), z + 3)
                && this.isAllAir(chunk, x, y, z, x, y + this.trunkHeight.lowerEndpoint() - 2, z))
            return false;
        Block block = chunk.get(x, height, z);
        return random.nextInt(40) == 0 && block == Blocks.GRASS_BLOCK;
    }

    private boolean isAllAir(RawChunk chunk, int x1, int y1, int z1, int x2, int y2, int z2) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                    if (!chunk.get(x, y, z).isAir()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

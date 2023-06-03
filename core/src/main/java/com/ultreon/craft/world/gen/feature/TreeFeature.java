package com.ultreon.craft.world.gen.feature;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.google.common.collect.Range;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.Chunk;
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
    public void generate(World world, Chunk chunk, int x, int y, int z, Random random) {
        chunk.setFast(x, y, z, Blocks.DIRT);

        int trunkHeight = random.nextInt(this.trunkHeight.lowerEndpoint(), this.trunkHeight.upperEndpoint());
        for (int ty = 0; ty < trunkHeight; ty++) {
            chunk.set(x, y + ty + 1, z, Blocks.LOG);
        }

        for (int lx = x - 2; lx <= x + 2; lx++) {
            for (int lz = z - 2; lz <= z + 2; lz++) {
                for (int ly = trunkHeight - 1; ly <= trunkHeight; ly++) {
                    if (lx == x && lz == z) continue;

                    chunk.set(lx, y + ly, lz, Blocks.LEAVES);
                }
            }
        }

        for (int ly = y + trunkHeight + 1; ly <= y + trunkHeight + 2; ly++) {
            chunk.set(x - 1, ly, z, Blocks.LEAVES);
            chunk.set(x + 1, ly, z, Blocks.LEAVES);
            chunk.set(x, ly, z - 1, Blocks.LEAVES);
            chunk.set(x, ly, z + 1, Blocks.LEAVES);
            chunk.set(x, ly, z, Blocks.LEAVES);
        }

        if (random.nextBoolean()) chunk.set(x - 1, y + trunkHeight + 1, z - 1, Blocks.LEAVES);
        if (random.nextBoolean()) chunk.set(x - 1, y + trunkHeight + 1, z + 1, Blocks.LEAVES);
        if (random.nextBoolean()) chunk.set(x + 1, y + trunkHeight + 1, z - 1, Blocks.LEAVES);
        if (random.nextBoolean()) chunk.set(x + 1, y + trunkHeight + 1, z + 1, Blocks.LEAVES);
    }

    @Override
    public boolean canGenerate(World world, Chunk chunk, int x, int y, int z, Random random) {
        int height = chunk.getHeight(x, z);
        if (y != height) return false;

        if (!world.collide(new BoundingBox(new Vector3(x - 2, y + (trunkHeight.lowerEndpoint() - 2), z - 2), new Vector3(x + 1, y + trunkHeight.upperEndpoint(), z + 1))).isEmpty()
                || !world.collide(new BoundingBox(new Vector3(x, y, z), new Vector3(x, y + trunkHeight.lowerEndpoint() - 2, z))).isEmpty())
            return false;
        Block block = chunk.get(x, height, z);
        return random.nextInt(10) == 0 && block == Blocks.GRASS_BLOCK;
    }
}

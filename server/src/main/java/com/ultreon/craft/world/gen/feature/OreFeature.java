package com.ultreon.craft.world.gen.feature;

import com.badlogic.gdx.math.GridPoint2;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.world.ChunkAccess;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.WorldGenFeature;
import com.ultreon.craft.world.rng.JavaRNG;
import com.ultreon.craft.world.rng.RNG;
import kotlin.ranges.IntRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OreFeature extends WorldGenFeature {
    private final Block ore;
    private final int chance;
    private final long seed;
    private final IntRange sizeRange;
    private final IntRange heightRange;

    public OreFeature(long seed, Block ore, int chance, IntRange sizeRange, IntRange heightRange) {
        super();
        this.seed = seed;

        this.ore = ore;
        this.chance = chance;
        this.sizeRange = sizeRange;
        this.heightRange = heightRange;
    }

    @Override
    public boolean handle(@NotNull World world, @NotNull ChunkAccess chunk, int x, int z, int height) {
        int posSeed = (x + chunk.getOffset().x) << 16 | (z + chunk.getOffset().z) & 0xFFFF;
        long seed = (world.getSeed() ^ this.seed << 32) ^ posSeed;

        RNG random = new JavaRNG(seed);

        if (height < this.heightRange.getStart()) return false;

        if (random.chance(this.chance)) {
            int y = random.randint(heightRange.getStart(), heightRange.getEndInclusive());

            if (chunk.get(x, y, z).getBlock() != Blocks.STONE) return false;

            int v = random.randint(sizeRange.getStart(), sizeRange.getEndInclusive());
            int xOffset = 0;
            int zOffset = 0;

            chunk.set(x + xOffset, y, z + zOffset, this.ore.createMeta());

            UltracraftServer.LOGGER.warn("Generating ore feature at: " + (x + chunk.getOffset().x) + ", " + (y + chunk.getOffset().y) + ", " + (z + chunk.getOffset().z));

            GridPoint2 offset = new GridPoint2(xOffset, zOffset);
            List<GridPoint2> offsets = new DefaultedArray<>(() -> new GridPoint2(0, 0), v);
            for (int i = 0; i < v; i++) {
                int attempts = 3;
                while (offsets.contains(offset) && attempts-- > 0) {
                    xOffset = random.randint(-(v / 2) - 1, (v / 2) + 1);
                    zOffset = random.randint(-(v / 2) - 1, (v / 2) + 1);
                    offset = new GridPoint2(xOffset, zOffset);
                }

                offsets.add(offset);
                chunk.set(x + xOffset, y, z + zOffset, this.ore.createMeta());
            }
            return true;
        }

        return false;
    }

    @Override
    public void create(@NotNull ServerWorld world) {

    }
}

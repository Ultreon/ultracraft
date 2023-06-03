package com.ultreon.craft.world.gen;

import java.util.Random;

public class WorldGenRandom extends Random {
    public WorldGenRandom() {
    }

    public WorldGenRandom(long seed) {
        super(seed);
    }

    public long setDecoStageSeed(long worldSeed, int pMinChunkBlockX, int pMinChunkBlockZ) {
        this.setSeed(worldSeed);
        long seedX = this.nextLong() | 1L;
        long seedZ = this.nextLong() | 1L;
        long decoStageSeed = (long)pMinChunkBlockX * seedX + (long)pMinChunkBlockZ * seedZ ^ worldSeed;
        this.setSeed(decoStageSeed);
        return decoStageSeed;
    }

    public void setFeatureSeed(long decoStageSeed, int index, int decoStage) {
        long i = decoStageSeed + (long)index + 10000L * decoStage;
        this.setSeed(i);
    }
}

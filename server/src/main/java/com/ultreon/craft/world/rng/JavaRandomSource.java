package com.ultreon.craft.world.rng;

import java.util.Random;

public class JavaRandomSource implements RandomSource {
    private final Random random;

    public JavaRandomSource() {
        this(new Random());
    }

    public JavaRandomSource(long seed) {
        this(new Random(seed));
    }

    public JavaRandomSource(Random random) {
        this.random = random;
    }

    @Override
    public int nextInt(int min, int max) {
        return this.random.nextInt(max - min) + min;
    }

    @Override
    public boolean chance(int max) {
        return this.random.nextInt(max + 1) == 0;
    }

    @Override
    public boolean chance(float chance) {
        return this.random.nextFloat() <= chance;
    }

    @Override
    public float randrange(float min, float max) {
        return this.random.nextFloat(min, max);
    }

    @Override
    public double randrange(double min, double max) {
        return this.random.nextDouble(min, max);
    }

    @Override
    public long nextLong() {
        return this.random.nextLong();
    }

    @Override
    public void setSeed(long seed) {
        this.random.setSeed(seed);
    }

    @Override
    public int nextInt(int length) {
        return this.random.nextInt(length);
    }
}

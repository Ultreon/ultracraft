package com.ultreon.craft.world.rng;

import java.util.Random;

public class JavaRNG implements RNG {
    private final Random random;

    public JavaRNG() {
        this(new Random());
    }

    public JavaRNG(long seed) {
        this(new Random(seed));
    }

    public JavaRNG(Random random) {
        this.random = random;
    }

    @Override
    public int randint(int min, int max) {
        return this.random.nextInt(min, max + 1);
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
}

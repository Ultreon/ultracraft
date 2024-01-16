package com.ultreon.craft.world.rng;

public interface RandomSource {
    default int nextInt(int min, int max) {
        return this.nextInt(max - min) + min;
    }

    int nextInt(int length);

    default int randint(int min, int max) {
        return this.nextInt(min, max + 1);
    }
    boolean chance(int max);
    boolean chance(float chance);
    float randrange(float min, float max);

    double randrange(double min, double max);

    default boolean nextBoolean() {
        return this.chance(1);
    }

    long nextLong();

    void setSeed(long seed);
}

package com.ultreon.craft.world.rng;

public interface RandomSource {
    int randint(int min, int max);
    boolean chance(int max);
    boolean chance(float chance);
    float randrange(float min, float max);
    double randrange(double min, double max);
}

package com.ultreon.craft.world.gen;

import de.articdive.jnoise.core.api.noisegen.NoiseGenerator;
import de.articdive.jnoise.generators.noisegen.opensimplex.SuperSimplexNoiseGenerator;

/**
 * This is a noise generator that generates cave-like terrain.
 * Values are either zero or one. Where 1 is air and 0 is solid.
 *
 * @author XyperCode
 */
public class CaveNoiseGenerator implements NoiseGenerator {
    public static final double SCALE = 1;
    public static final double THRESHOLD = -0.4;
    private final SuperSimplexNoiseGenerator baseNoise;

    public CaveNoiseGenerator(long seed) {
        this.baseNoise = SuperSimplexNoiseGenerator.newBuilder().setSeed(seed).build();
    }

    @Override
    public double evaluateNoise(double x) {
        return baseNoise.evaluateNoise(x) < THRESHOLD ? 1 : 0;
    }

    @Override
    public double evaluateNoise(double x, double y) {
        return baseNoise.evaluateNoise(x / SCALE, y / SCALE) < THRESHOLD ? 1 : 0;
    }

    @Override
    public double evaluateNoise(double x, double y, double z) {
        return baseNoise.evaluateNoise(x / SCALE, y / SCALE, z / SCALE) < THRESHOLD ? 1 : 0;
    }

    @Override
    public double evaluateNoise(double x, double y, double z, double w) {
        return baseNoise.evaluateNoise(x / SCALE, y / SCALE, z / SCALE, w / SCALE) <  THRESHOLD ? 1 : 0;
    }
}

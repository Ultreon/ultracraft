package com.ultreon.craft.world.gen;

import com.ultreon.craft.world.gen.noise.FastNoiseLite;
import de.articdive.jnoise.core.api.functions.Interpolation;
import de.articdive.jnoise.core.api.noisegen.NoiseGenerator;
import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;

import static java.lang.Double.sum;
import static java.lang.Math.abs;

/**
 * This is a noise generator that generates cave-like terrain.
 * Values are either zero or one. Where 1 is air and 0 is solid.
 *
 * @author XyperCode
 */
public class CaveNoiseGenerator implements NoiseGenerator {
    public static final double SCALE = 24;
    public static final double THRESHOLD = 0.4;
    private final JNoise baseNoise;
    private final JNoise baseNoise2;

    public CaveNoiseGenerator(long seed) {
        this.baseNoise = newNoiseBuilder(seed).build();
        this.baseNoise2 = newNoiseBuilder(seed + 1).build();
    }

    private static JNoise.JNoiseBuilder<?> newNoiseBuilder(long seed) {
        return JNoise.newBuilder()
                .perlin(seed, Interpolation.LINEAR, FadeFunction.QUINTIC_POLY)
                .octavate(2, 10, 2, FractalFunction.TURBULENCE, false)
                .scale(1 / SCALE)
                .abs()
                ;
    }

    @Override
    public double evaluateNoise(double x) {
        return baseNoise.evaluateNoise(x);
    }

    @Override
    public double evaluateNoise(double x, double y) {
        return baseNoise.evaluateNoise(x, y);
    }

    @Override
    public double evaluateNoise(double x, double y, double z) {
        return abs(baseNoise.evaluateNoise(x, y, z, baseNoise2.evaluateNoise(x, y, z))) > THRESHOLD ? 1 : 0;
    }

    @Override
    public double evaluateNoise(double x, double y, double z, double w) {
        return baseNoise.evaluateNoise(x, y, z, w) > THRESHOLD ? 1 : 0;
    }
}

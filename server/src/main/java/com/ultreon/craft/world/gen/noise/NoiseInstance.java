package com.ultreon.craft.world.gen.noise;

import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.craft.server.ServerDisposable;
import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import org.jetbrains.annotations.UnknownNullability;

public class NoiseInstance implements ServerDisposable {
    public static final NoiseInstance ZERO = new ZeroNoiseInstance();
    @UnknownNullability
    private NoiseSource noise;
    private final long seed;
    private final double noiseZoom;
    private final double octaves;
    private final Vec2f offset;
    private final double redistributionModifier;
    private final double exponent;
    private final double persistence;
    private final double amplitude;
    private final double base;

    public NoiseInstance(NoiseSource noise, long seed, double noiseZoom, double octaves, Vec2f offset, double redistributionModifier, double exponent, double persistence, double amplitude, double base) {
        this.noise = noise;
        this.seed = seed;
        this.noiseZoom = noiseZoom;
        this.octaves = octaves;
        this.offset = offset;
        this.redistributionModifier = redistributionModifier;
        this.exponent = exponent;
        this.persistence = persistence;
        this.amplitude = amplitude;
        this.base = base;
    }

    public double eval(double x) {
        return this.noise.evaluateNoise(x);
    }

    public double eval(double x, double y) {
        return this.noise.evaluateNoise(x, y);
    }

    public double eval(double x, double y, double z) {
        return this.noise.evaluateNoise(x, y, z);
    }

    public double eval(double x, double y, double z, double w) {
        return this.noise.evaluateNoise(x, y, z, w);
    }

    public long seed() {
        return this.seed;
    }

    public double noiseZoom() {
        return this.noiseZoom;
    }

    public double octaves() {
        return this.octaves;
    }

    public Vec2f offset() {
        return this.offset;
    }

    public double redistributionModifier() {
        return this.redistributionModifier;
    }

    public double exponent() {
        return this.exponent;
    }

    public double persistence() {
        return this.persistence;
    }

    @Override
    public void dispose() {
        this.noise = null;
    }

    public double amplitude() {
        return this.amplitude;
    }

    public double base() {
        return this.base;
    }

    private static class ZeroNoiseInstance extends NoiseInstance {
        public ZeroNoiseInstance() {
            super(new ZeroNoiseGenerator(), 0, 0, 0, new Vec2f(0, 0), 0, 0, 0, 0, 0);
        }

        private static class ZeroNoiseGenerator implements NoiseSource {
            @Override
            public double evaluateNoise(double x) {
                return 0;
            }

            @Override
            public double evaluateNoise(double x, double y) {
                return 0;
            }

            @Override
            public double evaluateNoise(double x, double y, double z) {
                return 0;
            }

            @Override
            public double evaluateNoise(double x, double y, double z, double w) {
                return 0;
            }
        }
    }
}

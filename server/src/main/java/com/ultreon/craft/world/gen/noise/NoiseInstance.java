package com.ultreon.craft.world.gen.noise;

import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.libs.commons.v0.vector.Vec2f;

public class NoiseInstance implements ServerDisposable {
    private final NoiseType noise;
    private final long seed;
    private final double noiseZoom;
    private final double octaves;
    private final Vec2f offset;
    private final double redistributionModifier;
    private final double exponent;
    private final double persistence;
    private final double amplitude;
    private final double base;

    public NoiseInstance(NoiseType noise, long seed, double noiseZoom, double octaves, Vec2f offset, double redistributionModifier, double exponent, double persistence, double amplitude, double base) {
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

    public double eval(double x, double y) {
        return this.noise.eval(x, y);
    }

    public double eval(double x, double y, double z) {
        return this.noise.eval(x, y, z);
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
        this.noise.dispose();
    }

    public double amplitude() {
        return this.amplitude;
    }

    public double base() {
        return this.base;
    }
}

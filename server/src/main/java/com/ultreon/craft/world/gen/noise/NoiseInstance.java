package com.ultreon.craft.world.gen.noise;

import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.craft.server.ServerDisposable;

public class NoiseInstance implements ServerDisposable {
    private final NoiseType noise;
    private final long seed;
    private final float noiseZoom;
    private final float octaves;
    private final Vec2f offset;
    private final float redistributionModifier;
    private final float exponent;
    private final float persistence;
    private final float amplitude;
    private final float base;

    public NoiseInstance(NoiseType noise, long seed, float noiseZoom, float octaves, Vec2f offset, float redistributionModifier, float exponent, float persistence, float amplitude, float base) {
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

    public float noiseZoom() {
        return this.noiseZoom;
    }

    public float octaves() {
        return this.octaves;
    }

    public Vec2f offset() {
        return this.offset;
    }

    public float redistributionModifier() {
        return this.redistributionModifier;
    }

    public float exponent() {
        return this.exponent;
    }

    public float persistence() {
        return this.persistence;
    }

    @Override
    public void dispose() {
        this.noise.dispose();
    }

    public float amplitude() {
        return this.amplitude;
    }

    public float base() {
        return this.base;
    }
}

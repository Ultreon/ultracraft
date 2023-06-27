package com.ultreon.craft.world.gen.noise;

import com.badlogic.gdx.math.Vector2;

import java.util.Objects;

public final class NoiseSettings {
    private final float noiseZoom;
    private final int octaves;
    private final Vector2 offset;
    private long seed;
    private final float persistence;
    private final float redistributionModifier;
    private final float exponent;

    public NoiseSettings(float noiseZoom, int octaves, Vector2 offset, long seed, float persistence,
                         float redistributionModifier, float exponent) {
        this.noiseZoom = noiseZoom;
        this.octaves = octaves;
        this.offset = offset;
        this.seed = seed;
        this.persistence = persistence;
        this.redistributionModifier = redistributionModifier;
        this.exponent = exponent;
    }

    public NoiseInstance create(long seed) {
        this.seed = seed;

        return new NoiseInstance(new SimplexNoise((int)Math.pow(2, this.octaves), this.persistence, seed), seed,
                this.noiseZoom, this.octaves, this.offset, this.redistributionModifier, this.exponent, this.persistence);
    }

    public float noiseZoom() {
        return this.noiseZoom;
    }

    public int octaves() {
        return this.octaves;
    }

    public Vector2 offset() {
        return this.offset;
    }

    public long seed() {
        return this.seed;
    }

    public float persistence() {
        return this.persistence;
    }

    public float redistributionModifier() {
        return this.redistributionModifier;
    }

    public float exponent() {
        return this.exponent;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        NoiseSettings that = (NoiseSettings) obj;
        return Float.floatToIntBits(this.noiseZoom) == Float.floatToIntBits(that.noiseZoom) &&
                this.octaves == that.octaves &&
                Objects.equals(this.offset, that.offset) &&
                Objects.equals(this.seed, that.seed) &&
                Float.floatToIntBits(this.persistence) == Float.floatToIntBits(that.persistence) &&
                Float.floatToIntBits(this.redistributionModifier) == Float.floatToIntBits(that.redistributionModifier) &&
                Float.floatToIntBits(this.exponent) == Float.floatToIntBits(that.exponent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.noiseZoom, this.octaves, this.offset, this.seed, this.persistence, this.redistributionModifier, this.exponent);
    }

    @Override
    public String toString() {
        return "NoiseSettings[" +
                "noiseZoom=" + this.noiseZoom + ", " +
                "octaves=" + this.octaves + ", " +
                "offset=" + this.offset + ", " +
                "worldOffset=" + this.seed + ", " +
                "persistence=" + this.persistence + ", " +
                "redistributionModifier=" + this.redistributionModifier + ", " +
                "exponent=" + this.exponent + ']';
    }

}

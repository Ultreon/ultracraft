package com.ultreon.craft.world.gen.noise;

import com.ultreon.craft.util.Vec2i;

import java.util.Objects;

public final class NoiseSettings {
    private final float noiseZoom;
    private final int octaves;
    private final Vec2i offset;
    public Vec2i worldOffset;
    private final float persistence;
    private final float redistributionModifier;
    private final float exponent;

    public NoiseSettings(float noiseZoom, int octaves, Vec2i offset, Vec2i worldOffset, float persistence,
                         float redistributionModifier, float exponent) {
        this.noiseZoom = noiseZoom * 0.0000001f;
        this.octaves = octaves;
        this.offset = offset;
        this.worldOffset = worldOffset;
        this.persistence = persistence;
        this.redistributionModifier = redistributionModifier;
        this.exponent = exponent;
    }

    public float noiseZoom() {
        return noiseZoom;
    }

    public int octaves() {
        return octaves;
    }

    public Vec2i offset() {
        return offset;
    }

    public Vec2i worldOffset() {
        return worldOffset;
    }

    public float persistence() {
        return persistence;
    }

    public float redistributionModifier() {
        return redistributionModifier;
    }

    public float exponent() {
        return exponent;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (NoiseSettings) obj;
        return Float.floatToIntBits(this.noiseZoom) == Float.floatToIntBits(that.noiseZoom) &&
                this.octaves == that.octaves &&
                Objects.equals(this.offset, that.offset) &&
                Objects.equals(this.worldOffset, that.worldOffset) &&
                Float.floatToIntBits(this.persistence) == Float.floatToIntBits(that.persistence) &&
                Float.floatToIntBits(this.redistributionModifier) == Float.floatToIntBits(that.redistributionModifier) &&
                Float.floatToIntBits(this.exponent) == Float.floatToIntBits(that.exponent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noiseZoom, octaves, offset, worldOffset, persistence, redistributionModifier, exponent);
    }

    @Override
    public String toString() {
        return "NoiseSettings[" +
                "noiseZoom=" + noiseZoom + ", " +
                "octaves=" + octaves + ", " +
                "offset=" + offset + ", " +
                "worldOffset=" + worldOffset + ", " +
                "persistence=" + persistence + ", " +
                "redistributionModifier=" + redistributionModifier + ", " +
                "exponent=" + exponent + ']';
    }

}

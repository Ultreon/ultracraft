package com.ultreon.craft.world.gen.noise;

import com.ultreon.libs.commons.v0.vector.Vec2f;
import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex2DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex3DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex4DVariant;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;

import java.util.Objects;

public final class NoiseConfig {
    private final float noiseZoom;
    private final float octaves;
    private final Vec2f offset;
    private long seed;
    private final float persistence;
    private final float redistributionModifier;
    private final float exponent;
    private final float amplitude;
    private final float base;

    /**
     * @param noiseZoom              the zoom of the noise
     * @param octaves                the roughness
     * @param offset
     * @param seed
     * @param persistence
     * @param redistributionModifier
     * @param exponent
     * @param amplitude
     * @param base
     */
    public NoiseConfig(float noiseZoom, float octaves, Vec2f offset, long seed, float persistence,
                       float redistributionModifier, float exponent, float amplitude, float base) {
        this.noiseZoom = noiseZoom;
        this.octaves = octaves;
        this.offset = offset;
        this.seed = seed;
        this.persistence = persistence;
        this.redistributionModifier = redistributionModifier;
        this.exponent = exponent;
        this.amplitude = amplitude;
        this.base = base;
    }

    public NoiseInstance create(long seed) {
        this.seed = seed;

        return new NoiseInstance(new JNoiseType(JNoise.newBuilder()
                .fastSimplex(seed, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                .scale(this.noiseZoom)
                .octavate((int) this.octaves, 1, 1, FractalFunction.FBM, true)
//                .addModifier(v -> Math.pow(v, this.exponent))
                .clamp(0, 1)
                .addModifier(v -> (v * this.amplitude) + this.base)
                .build()), seed);
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
        NoiseConfig that = (NoiseConfig) obj;
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
        return "NoiseConfig[" +
                "noiseZoom=" + this.noiseZoom + ", " +
                "octaves=" + this.octaves + ", " +
                "offset=" + this.offset + ", " +
                "worldOffset=" + this.seed + ", " +
                "persistence=" + this.persistence + ", " +
                "redistributionModifier=" + this.redistributionModifier + ", " +
                "exponent=" + this.exponent + ']';
    }

}

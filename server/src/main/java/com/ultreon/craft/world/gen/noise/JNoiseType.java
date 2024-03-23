package com.ultreon.craft.world.gen.noise;

import de.articdive.jnoise.core.api.pipeline.NoiseSource;

public record JNoiseType(NoiseSource source) implements NoiseType {

    @Override
    public void dispose() {

    }

    @Override
    public double eval(double x, double y) {
        return source.evaluateNoise(x, y) + 1 / 2.0;
    }

    @Override
    public double eval(double x, double y, double z) {
        return source.evaluateNoise(x, y, z) + 1 / 2.0;
    }
}

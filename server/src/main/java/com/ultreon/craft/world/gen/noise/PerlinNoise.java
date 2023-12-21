package com.ultreon.craft.world.gen.noise;

import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import de.articdive.jnoise.generators.noisegen.opensimplex.SuperSimplexNoiseGenerator;
import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator;
import org.jetbrains.annotations.ApiStatus;

import java.util.Random;

/**
 * @deprecated Use {@link PerlinNoiseGenerator} instead.
 */
@Deprecated(since = "0.1.0")
public class PerlinNoise implements NoiseType {
	private PerlinNoiseGenerator noise;
	private final double seed;

    public PerlinNoise() {
		this(new Random().nextLong());
	}

	public PerlinNoise(long seed) {
		this.seed = seed;
		this.noise = PerlinNoiseGenerator.newBuilder().setSeed(seed).build();
	}

	public double getSeed() {
		return this.seed;
	}

	@Override
	public double eval(double x, double y, double z) {
		return (this.noise.evaluateNoise(x, y, z) + 0.5) / 2;
	}

	@Override
	public double eval(double x, double y) {
		return (this.noise.evaluateNoise(x, y) + 0.5) / 2;
	}

    public double eval(double x) {
		return (this.noise.evaluateNoise(x) + 0.5) / 2;
    }

	@Override
	@SuppressWarnings("DataFlowIssue")
	public void dispose() {
		this.noise = null;
    }
}
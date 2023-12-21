package com.ultreon.craft.world.gen.noise;

import com.ultreon.craft.server.ServerDisposable;
import de.articdive.jnoise.core.api.pipeline.NoiseSource;

/**
 * @deprecated Use {@link NoiseSource} instead.
 */
public interface NoiseType extends ServerDisposable {

    double eval(double x, double y);

    double eval(double x, double y, double z);
}

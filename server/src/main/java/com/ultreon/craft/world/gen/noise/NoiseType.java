package com.ultreon.craft.world.gen.noise;

import com.ultreon.craft.server.ServerDisposable;

public interface NoiseType extends ServerDisposable {

    double eval(double x, double y);

    double eval(double x, double y, double z);
}

package com.ultreon.craft.world.gen.noise;

import com.badlogic.gdx.utils.Disposable;

public interface NoiseType extends Disposable {

    double eval(double x, double y);

    double eval(double x, double y, double z);
}

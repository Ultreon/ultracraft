package com.ultreon.craft.util;

import com.badlogic.gdx.math.Vector3;

public enum Axis {
    X(Vector3.X),
    Y(Vector3.Y),
    Z(Vector3.Z);

    private final Vector3 vector;

    Axis(Vector3 vector) {
        this.vector = vector;
    }

    public Vector3 getVector() {
        return vector;
    }
}

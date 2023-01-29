package com.ultreon.craft.util;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import de.articdive.jnoise.core.util.vectors.Vector1D;
import de.articdive.jnoise.core.util.vectors.Vector2D;
import de.articdive.jnoise.core.util.vectors.Vector3D;
import de.articdive.jnoise.core.util.vectors.Vector4D;

public final class Mth extends UtilityClass {
    public static Vector2 round(Vector2 vec) {
        return new Vector2(Math.round(vec.x), Math.round(vec.y));
    }

    public static Vector3 round(Vector3 vec) {
        return new Vector3(Math.round(vec.x), Math.round(vec.y), Math.round(vec.z));
    }

    public static Vector1D round(Vector1D vec) {
        return new Vector1D(Math.round(vec.x()));
    }

    public static Vector2D round(Vector2D vec) {
        return new Vector2D(Math.round(vec.x()), Math.round(vec.y()));
    }

    public static Vector3D round(Vector3D vec) {
        return new Vector3D(Math.round(vec.x()), Math.round(vec.y()), Math.round(vec.z()));
    }

    public static Vector4D round(Vector4D vec) {
        return new Vector4D(Math.round(vec.x()), Math.round(vec.y()), Math.round(vec.z()), Math.round(vec.w()));
    }
}

package com.ultreon.craft.util;

import com.badlogic.gdx.math.GridPoint2;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public final class MathHelper extends UtilityClass {
    public static GridPoint2 round(Vector2 vec) {
        return new GridPoint2(Math.round(vec.x), Math.round(vec.y));
    }

    public static Vec3i round(Vector3 vec) {
        return new Vec3i(Math.round(vec.x), Math.round(vec.y), Math.round(vec.z));
    }
}

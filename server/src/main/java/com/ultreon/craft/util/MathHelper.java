package com.ultreon.craft.util;

import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import com.ultreon.libs.commons.v0.vector.Vec3i;

public final class MathHelper {
    public static Vec2i round(Vec2f vec) {
        return new Vec2i(Math.round(vec.x), Math.round(vec.y));
    }

    public static Vec3i round(Vec3f vec) {
        return new Vec3i(Math.round(vec.x), Math.round(vec.y), Math.round(vec.z));
    }
}

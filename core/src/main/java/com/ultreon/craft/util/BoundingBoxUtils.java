package com.ultreon.craft.util;

import com.badlogic.gdx.math.collision.BoundingBox;

public class BoundingBoxUtils {
    public static BoundingBox offset(BoundingBox boundingBox, float dx, float dy, float dz) {
        var b = new BoundingBox(boundingBox);
        b.min.add(dx, dy, dz);
        b.max.add(dx, dy, dz);
        b.update();
        return b;
    }
}

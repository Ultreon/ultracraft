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
    
    public static float clipXCollide(BoundingBox a, BoundingBox c, float xa) {
        float max;
        if (c.max.y <= a.min.y || c.min.y >= a.max.y) {
            return xa;
        }
        if (c.max.z <= a.min.z || c.min.z >= a.max.z) {
            return xa;
        }
        if (xa > 0.0f && c.max.x <= a.min.x && (max = a.min.x - c.max.x - 0.0f) < xa) {
            xa = max;
        }
        if (xa < 0.0f && c.min.x >= a.max.x && (max = a.max.x - c.min.x + 0.0f) > xa) {
            xa = max;
        }
        return xa;
    }

    public static float clipZCollide(BoundingBox a, BoundingBox c, float za) {
        float max;
        if (c.max.x <= a.min.x || c.min.x >= a.max.x) {
            return za;
        }
        if (c.max.y <= a.min.y || c.min.y >= a.max.y) {
            return za;
        }
        if (za > 0.0f && c.max.z <= a.min.z && (max = a.min.z - c.max.z - 0.0f) < za) {
            za = max;
        }
        if (za < 0.0f && c.min.z >= a.max.z && (max = a.max.z - c.min.z + 0.0f) > za) {
            za = max;
        }
        return za;
    }

    public static float clipYCollide(BoundingBox a, BoundingBox c, float ya) {
        float max;
        if (c.max.x <= a.min.x || c.min.x >= a.max.x) {
            return ya;
        }
        if (c.max.z <= a.min.z || c.min.z >= a.max.z) {
            return ya;
        }
        if (ya > 0.0f && c.max.y <= a.min.y && (max = a.min.y - c.max.y - 0.0f) < ya) {
            ya = max;
        }
        if (ya < 0.0f && c.min.y >= a.max.y && (max = a.max.y - c.min.y + 0.0f) > ya) {
            ya = max;
        }
        return ya;
    }

}

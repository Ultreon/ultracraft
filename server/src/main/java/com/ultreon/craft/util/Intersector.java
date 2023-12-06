package com.ultreon.craft.util;

import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.Nullable;

public class Intersector {
    private final static Vec3d tmp = new Vec3d();

    @SuppressWarnings({"UnqualifiedStaticUsage", "ConstantValue"})
    public static boolean intersectRayBounds (Ray ray, BoundingBox box, @Nullable Vec3d intersection) {
        if (box.contains(ray.origin)) {
            if (intersection != null) intersection.set(ray.origin);
            return true;
        }
        double lowest = 0, t;
        boolean hit = false;

        // min x
        if (ray.origin.x <= box.min.x && ray.direction.x > 0) {
            t = (box.min.x - ray.origin.x) / ray.direction.x;
            if (t >= 0) {
                tmp.set(ray.direction).mul(t).add(ray.origin);
                if (tmp.y >= box.min.y && tmp.y <= box.max.y && tmp.z >= box.min.z && tmp.z <= box.max.z && (!hit || t < lowest)) {
                    hit = true;
                    lowest = t;
                }
            }
        }
        // max x
        if (ray.origin.x >= box.max.x && ray.direction.x < 0) {
            t = (box.max.x - ray.origin.x) / ray.direction.x;
            if (t >= 0) {
                tmp.set(ray.direction).mul(t).add(ray.origin);
                if (tmp.y >= box.min.y && tmp.y <= box.max.y && tmp.z >= box.min.z && tmp.z <= box.max.z && (!hit || t < lowest)) {
                    hit = true;
                    lowest = t;
                }
            }
        }
        // min y
        if (ray.origin.y <= box.min.y && ray.direction.y > 0) {
            t = (box.min.y - ray.origin.y) / ray.direction.y;
            if (t >= 0) {
                tmp.set(ray.direction).mul(t).add(ray.origin);
                if (tmp.x >= box.min.x && tmp.x <= box.max.x && tmp.z >= box.min.z && tmp.z <= box.max.z && (!hit || t < lowest)) {
                    hit = true;
                    lowest = t;
                }
            }
        }
        // max y
        if (ray.origin.y >= box.max.y && ray.direction.y < 0) {
            t = (box.max.y - ray.origin.y) / ray.direction.y;
            if (t >= 0) {
                tmp.set(ray.direction).mul(t).add(ray.origin);
                if (tmp.x >= box.min.x && tmp.x <= box.max.x && tmp.z >= box.min.z && tmp.z <= box.max.z && (!hit || t < lowest)) {
                    hit = true;
                    lowest = t;
                }
            }
        }
        // min z
        if (ray.origin.z <= box.min.z && ray.direction.z > 0) {
            t = (box.min.z - ray.origin.z) / ray.direction.z;
            if (t >= 0) {
                tmp.set(ray.direction).mul(t).add(ray.origin);
                if (tmp.x >= box.min.x && tmp.x <= box.max.x && tmp.y >= box.min.y && tmp.y <= box.max.y && (!hit || t < lowest)) {
                    hit = true;
                    lowest = t;
                }
            }
        }
        // max z
        if (ray.origin.z >= box.max.z && ray.direction.z < 0) {
            t = (box.max.z - ray.origin.z) / ray.direction.z;
            if (t >= 0) {
                tmp.set(ray.direction).mul(t).add(ray.origin);
                if (tmp.x >= box.min.x && tmp.x <= box.max.x && tmp.y >= box.min.y && tmp.y <= box.max.y && (!hit || t < lowest)) {
                    hit = true;
                    lowest = t;
                }
            }
        }
        if (hit && intersection != null) {
            intersection.set(ray.direction).mul(lowest).add(ray.origin);
            if (intersection.x < box.min.x) {
                intersection.x = box.min.x;
            } else if (intersection.x > box.max.x) {
                intersection.x = box.max.x;
            }
            if (intersection.y < box.min.y) {
                intersection.y = box.min.y;
            } else if (intersection.y > box.max.y) {
                intersection.y = box.max.y;
            }
            if (intersection.z < box.min.z) {
                intersection.z = box.min.z;
            } else if (intersection.z > box.max.z) {
                intersection.z = box.max.z;
            }
        }
        return hit;
    }
}

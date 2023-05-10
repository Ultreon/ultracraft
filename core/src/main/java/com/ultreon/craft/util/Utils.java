package com.ultreon.craft.util;

import com.badlogic.gdx.math.Vector3;

public class Utils {
    public static Vector3 getTraceIntersection(Vector3 start1, Vector3 end1, Vector3 start2, Vector3 end2) {
        Vector3 dir1 = end1.sub(start1);
        Vector3 dir2 = end2.sub(start2);

        float a = dir1.dot(dir1);
        float b = dir1.dot(dir2);
        float c = dir2.dot(dir2);
        float d = dir1.x * (start1.x - start2.x) + dir1.y * (start1.y - start2.y) + dir1.z * (start1.z - start2.z);
        float e = dir2.x * (start1.x - start2.x) + dir2.y * (start1.y - start2.y) + dir2.z * (start1.z - start2.z);

        float det = a * c - b * b;

        if (det == 0) {
            return null;
        } else {
            float s = (b * e - c * d) / det;
            float t = (a * e - b * d) / det;

            if (s < 0 || s > 1 || t < 0 || t > 1) {
                return null;
            } else {
                return start1.add(dir1.x * s, dir1.y * s, dir1.z * s);
            }
        }
    }
}

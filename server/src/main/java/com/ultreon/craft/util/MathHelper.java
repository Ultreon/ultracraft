package com.ultreon.craft.util;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import com.ultreon.libs.commons.v0.vector.Vec3i;

public final class MathHelper {
    private static final String EX_INVALID_NUMBER_OF_VERTICES = "Invalid number of vertices";

    public static Vec2i round(Vec2f vec) {
        return new Vec2i(Math.round(vec.x), Math.round(vec.y));
    }

    public static Vec3i round(Vec3f vec) {
        return new Vec3i(Math.round(vec.x), Math.round(vec.y), Math.round(vec.z));
    }

    /**
     * Linear interpolation between two values.
     *
     * @param value The value to interpolate.
     * @param min The minimum value.
     * @param max The maximum value.
     * @return The interpolated value.
     */
    public static float lerp(float value, float min, float max) {
        return ((max - min) * value + min);
    }

    /**
     * Linear interpolation between two values.
     *
     * @param value The value to interpolate.
     * @param min   The minimum value.
     * @param max   The maximum value.
     * @return The interpolated value.
     */
    public static double lerp(double value, double min, double max) {
        return ((max - min) * value + min);
    }

    /**
     * Randomly rotates the given vertices.
     *
     * @param vertices The vertices to rotate.
     * @return The rotated vertices.
     */
    public static float[] rotate(float[] vertices, Vector3 origin, Axis axis) {
        // Rotate the vertices.
        FaceRotation rotation = FaceRotation.values()[MathUtils.random(0, FaceRotation.values().length - 1)];
        return switch (rotation) {
            case UNCHANGED -> vertices;
            case DEGREES_90 -> MathHelper.rotate(vertices, 90 * MathUtils.degRad, origin, axis);
            case DEGREES_180 -> MathHelper.rotate(vertices, 180 * MathUtils.degRad, origin, axis);
            case DEGREES_270 -> MathHelper.rotate(vertices, 270 * MathUtils.degRad, origin, axis);
        };
    }

    private static float[] rotate(float[] vertices, float angle, Vector3 origin, Axis axis) {
        switch (axis) {
            case X -> MathHelper.rotateX(vertices, angle, origin);
            case Y -> MathHelper.rotateY(vertices, angle, origin);
            case Z -> MathHelper.rotateZ(vertices, angle, origin);
        }
        return vertices;
    }

    // Function to rotate vertices around the X-axis with respect to a given origin
    public static void rotateX(float[] vertices, float angleInRadians, Vector3 origin) {
        if (vertices.length % 3 != 0) {
            throw new IllegalArgumentException(MathHelper.EX_INVALID_NUMBER_OF_VERTICES);
        }

        float cosTheta = (float) Math.cos(angleInRadians);
        float sinTheta = (float) Math.sin(angleInRadians);

        for (int i = 0; i < vertices.length; i += 3) {
            // Translate the vertices to bring the origin to the desired point
            float yTranslated = vertices[i + 1] - origin.y;
            float zTranslated = vertices[i + 2] - origin.z;

            // Apply rotation matrix with translation
            vertices[i + 1] = yTranslated * cosTheta - zTranslated * sinTheta + origin.y;
            vertices[i + 2] = yTranslated * sinTheta + zTranslated * cosTheta + origin.z;
        }
    }

    // Function to rotate vertices around the Z-axis with respect to a given origin

    /**
     * Rotates the given vertices around the Y axis.
     *
     * @param vertices The vertices to rotate.
     * @param angle    The angle to rotate the vertices (in radians).
     * @param origin   The origin point of the rotation.
     */
    public static void rotateY(float[] vertices, float angle, Vector3 origin) {
        if (vertices.length % 3 != 0) {
            throw new IllegalArgumentException(MathHelper.EX_INVALID_NUMBER_OF_VERTICES);
        }

        float cosTheta = (float) Math.cos(angle);
        float sinTheta = (float) Math.sin(angle);

        for (int i = 0; i < vertices.length; i += 3) {
            // Translate the vertices to bring the origin to the desired point
            float xTranslated = vertices[i] - origin.x;
            float zTranslated = vertices[i + 2] - origin.z;

            // Apply rotation matrix with translation
            vertices[i] = xTranslated * cosTheta - zTranslated * sinTheta + origin.x;
            vertices[i + 2] = xTranslated * sinTheta + zTranslated * cosTheta + origin.z;
        }
    }

    public static void rotateZ(float[] vertices, float angleInRadians, Vector3 origin) {
        if (vertices.length % 3 != 0) {
            throw new IllegalArgumentException(EX_INVALID_NUMBER_OF_VERTICES);
        }

        float cosTheta = (float) Math.cos(angleInRadians);
        float sinTheta = (float) Math.sin(angleInRadians);

        for (int i = 0; i < vertices.length; i += 3) {
            // Translate the vertices to bring the origin to the desired point
            float xTranslated = vertices[i] - origin.x;
            float yTranslated = vertices[i + 1] - origin.y;

            // Apply rotation matrix with translation
            vertices[i] = xTranslated * cosTheta - yTranslated * sinTheta + origin.x;
            vertices[i + 1] = xTranslated * sinTheta + yTranslated * cosTheta + origin.y;
        }
    }

    public enum FaceRotation {
        UNCHANGED,
        DEGREES_90,
        DEGREES_180,
        DEGREES_270
    }
}

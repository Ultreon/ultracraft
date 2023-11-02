package com.ultreon.craft.util;

import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import com.ultreon.libs.commons.v0.vector.Vec3i;

import java.util.Random;

public final class MathHelper {
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
     * Randomly rotates the given vertices.
     *
     * @param vertices The vertices to rotate.
     * @param random The random number generator.
     * @return The rotated vertices.
     */
    public static float[] rotate(float[] vertices, Random random) {
        // Rotate the vertices.
        FaceRotation rotation = FaceRotation.values()[random.nextInt(FaceRotation.values().length)];
        return switch (rotation) {
            case UNCHANGED -> vertices;
            case DEGREES_90 -> MathHelper.rotate90(vertices);
            case DEGREES_180 -> MathHelper.rotate180(vertices);
            case DEGREES_270 -> MathHelper.rotate270(vertices);
        };
    }

    private static float[] rotate90(float[] vertices) {
        // Rotate 90 degrees.
        float[] rotated = new float[vertices.length];
        for (int i = 0; i < vertices.length; i += 3) {
            rotated[i] = vertices[i + 2];
            rotated[i + 1] = vertices[i + 1];
            rotated[i + 2] = vertices[i];
        }
        return rotated;
    }

    private static float[] rotate180(float[] vertices) {
        // Rotate 180 degrees.
        float[] rotated = new float[vertices.length];
        for (int i = 0; i < vertices.length; i += 3) {
            rotated[i] = vertices[i];
            rotated[i + 1] = vertices[i + 2];
            rotated[i + 2] = vertices[i + 1];
        }
        return rotated;
    }

    private static float[] rotate270(float[] vertices) {
        // Rotate 270 degrees.
        float[] rotated = new float[vertices.length];
        for (int i = 0; i < vertices.length; i += 3) {
            rotated[i] = vertices[i + 1];
            rotated[i + 1] = vertices[i];
            rotated[i + 2] = vertices[i + 2];
        }
        return rotated;
    }

    public enum FaceRotation {
        UNCHANGED,
        DEGREES_90,
        DEGREES_180,
        DEGREES_270
    }
}

package com.ultreon.craft.world.gen.noise;

import com.ultreon.libs.commons.v0.vector.Vec2f;

public class NoiseUtils {
    private NoiseUtils() {
    }

    public static double remapValue(double value, double initialMin, double initialMax, double outputMin, double outputMax) {
        return outputMin + (value - initialMin) * (outputMax - outputMin) / (initialMax - initialMin);
    }

    public static double remapValue01(double value, double outputMin, double outputMax) {
        return outputMin + (value - 0) * (outputMax - outputMin);
    }

    public static int remapValue01ToInt(double value, double outputMin, double outputMax) {
        return (int) remapValue01(value, outputMin, outputMax);
    }

    public static double redistribution(double noise, NoiseInstance settings) {
        return Math.pow(noise * settings.redistributionModifier(), settings.exponent());
    }

    public static double octavePerlin(double x, double z, NoiseInstance settings) {
        double zoom = settings.noiseZoom();
        x *= zoom;
        z *= zoom;
        x += zoom;
        z += zoom;

        Vec2f offset = settings.offset();

        double total = 0.0F;
        double frequency = 1.0F;
        double amplitude = 1.0F;
        double amplitudeSum = 0.0F;  // Used for normalizing result to 0.0 - 1.0 range

        for (int i = 0; i < settings.octaves(); i++) {
            total += settings.eval((offset.x + x) * frequency, (offset.y + z) * frequency) * amplitude;

            amplitudeSum += amplitude;

            amplitude *= settings.persistence();
            frequency *= 2;
        }

        total *= settings.amplitude() / settings.octaves() / amplitudeSum;
        total += settings.base();

        return total;
    }
}
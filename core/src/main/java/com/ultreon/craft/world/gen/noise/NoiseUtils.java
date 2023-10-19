package com.ultreon.craft.world.gen.noise;

import com.badlogic.gdx.math.Vector2;
import com.ultreon.craft.util.UtilityClass;

public class NoiseUtils extends UtilityClass {
    private NoiseUtils() {
    }

    public static float remapValue(float value, float initialMin, float initialMax, float outputMin, float outputMax) {
        return outputMin + (value - initialMin) * (outputMax - outputMin) / (initialMax - initialMin);
    }

    public static float remapValue01(float value, float outputMin, float outputMax) {
        return outputMin + (value - 0) * (outputMax - outputMin);
    }

    public static int remapValue01ToInt(float value, float outputMin, float outputMax) {
        return (int) remapValue01(value, outputMin, outputMax);
    }

    public static float redistribution(float noise, NoiseInstance settings) {
        return (float) Math.pow(noise * settings.redistributionModifier(), settings.exponent());
    }

    public static float octavePerlin(float x, float z, NoiseInstance settings) {
        float zoom = settings.noiseZoom();
        x *= zoom;
        z *= zoom;
        x += zoom;
        z += zoom;

        Vector2 offset = settings.offset();

        float total = 0.0F;
        float frequency = 1.0F;
        float amplitude = 1.0F;
        float amplitudeSum = 0.0F;  // Used for normalizing result to 0.0 - 1.0 range

        for (int i = 0; i < settings.octaves(); i++) {
            total += settings.eval((offset.x + x) * frequency, (offset.y + z) * frequency) * amplitude;

            amplitudeSum += amplitude;

            amplitude *= settings.persistence();
            frequency *= 2;
        }

        return (total / amplitudeSum + 1) / 2;
    }

    private static class Flags {
        public static boolean enableWorldGenLogging = false;
    }
}
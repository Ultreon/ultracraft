package com.ultreon.craft.world.gen.noise;

public class MyNoise {
    public static float remapValue(float value, float initialMin, float initialMax, float outputMin, float outputMax) {
        return outputMin + (value - initialMin) * (outputMax - outputMin) / (initialMax - initialMin);
    }

    public static float remapValue01(float value, float outputMin, float outputMax) {
        return outputMin + (value - 0) * (outputMax - outputMin);
    }

    public static int remapValue01ToInt(float value, float outputMin, float outputMax) {
        return (int) remapValue01(value, outputMin, outputMax);
    }

    public static float redistribution(float noise, NoiseSettings settings) {
        return (float) Math.pow(noise * settings.redistributionModifier(), settings.exponent());
    }

    public static float octavePerlin(float x, float z, NoiseSettings settings) {
        x *= settings.noiseZoom();
        z *= settings.noiseZoom();
        x += settings.noiseZoom();
        z += settings.noiseZoom();

        float total = 0;
        float frequency = 1;
        float amplitude = 1;
        float amplitudeSum = 0;  // Used for normalizing result to 0.0 - 1.0 range
        for (int i = 0; i < settings.octaves(); i++) {
            total += new NoiseGenerator().noise((settings.offset().x + settings.worldOffset.x + x) * frequency, (settings.offset().y + settings.worldOffset.y + z) * frequency, 1) * amplitude;

            amplitudeSum += amplitude;

            amplitude *= settings.persistence();
            frequency *= 2;
        }

        return total / amplitudeSum;
    }
}
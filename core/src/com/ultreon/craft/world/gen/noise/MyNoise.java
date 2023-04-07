package com.ultreon.craft.world.gen.noise;

import com.ultreon.craft.debug.Debugger;
import de.articdive.jnoise.generators.noisegen.perlin.PerlinNoiseGenerator;

import java.util.Random;

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
        float frequency = 1f;
        float amplitude = 1f;
        float amplitudeSum = 0;  // Used for normalizing result to 0.0 - 1.0 range
        if (Flags.enableWorldGenLogging) {
            System.out.println(">>------- START -------<<");
        }
        for (int i = 0; i < settings.octaves(); i++) {
            long seed = settings.seed;
//            if (Flags.enableWorldGenLogging) {
//                System.out.println("seed = " + seed);
//            }
            float x2 = settings.offset().x;
            float z2 = settings.offset().y;
//            if (Flags.enableWorldGenLogging) {
//                System.out.println("x2 = " + x2 + ", z2 = " + z2);
//                System.out.println("x  = " + x + ", z  = " + z);
//            }
            float x1 = (x2 + seed + x) * frequency;
            float z1 = (z2 + seed + z) * frequency;
//            if (Flags.enableWorldGenLogging) {
//                System.out.println("x1 = " + x1 + ", z1 = " + z1);
//            }
            double evaluated = new OpenSimplexNoise(seed).eval(x1, z1, 1) * amplitude;
//            if (Flags.enableWorldGenLogging) {
//                System.out.println("amplitude = " + amplitude);
//                System.out.println("i = " + i + ", evaluated = " + evaluated);
//            }
            Debugger.evaluatedMax = Math.max(evaluated, Debugger.evaluatedMax);
            total += evaluated;
            if (Flags.enableWorldGenLogging) {
                System.out.println("total = " + total);
            }

            amplitudeSum += amplitude;
            if (Flags.enableWorldGenLogging) {
                System.out.println("amplitudeSum = " + amplitudeSum);
            }

            amplitude *= settings.persistence();
            if (Flags.enableWorldGenLogging) {
                System.out.println("amplitude = " + amplitude);
            }
            frequency *= 2;

            if (Flags.enableWorldGenLogging) {
                System.out.println("frequency = " + frequency);
            }
        }
        float octave = total / amplitudeSum;
        if (Flags.enableWorldGenLogging) {
            System.out.println("octave = " + octave);
        }
        if (Flags.enableWorldGenLogging) {
            System.out.println(">>-------- END --------<<");
        }
        Debugger.octaveMax = Math.max(octave, Debugger.octaveMax);

//        new RuntimeException("" + octave).printStackTrace();

        return octave;
    }

    private static class Flags {
        public static boolean enableWorldGenLogging = false;
    }
}
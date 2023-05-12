package com.ultreon.craft.world.gen

import com.ultreon.craft.*
import kotlin.math.*


object MyNoise {
    fun remapValue(value: Float, initialMin: Float, initialMax: Float, outputMin: Float, outputMax: Float): Float {
        return outputMin + (value - initialMin) * (outputMax - outputMin) / (initialMax - initialMin)
    }

    fun remapValue(value: Float, outputMin: Float, outputMax: Float): Float {
        return outputMin + (value - 0) * (outputMax - outputMin) / (1 - 0)
    }

    fun remapValueInt(value: Float, outputMin: Float, outputMax: Float): Int {
        return remapValue(value, outputMin, outputMax).toInt()
    }

    fun redistribution(noise: Float, settings: NoiseSettings): Float {
        return (noise * settings.redistributionModifier).pow(settings.exponent)
    }

    fun octaveSimplex(x: Float, z: Float, settings: NoiseSettings): Float {
        var x1 = x
        var z1 = z
        x1 *= settings.noiseZoom
        z1 *= settings.noiseZoom
        x1 += settings.noiseZoom
        z1 += settings.noiseZoom
        var total = 0f
        var frequency = 1f
        var amplitude = 1f
        var amplitudeSum = 0f // Used for normalizing result to 0.0 - 1.0 range
        for (i in 0 until settings.octaves) {
            total += (SimplexNoise.noise(
                (settings.offest.x + settings.worldOffset.x + x1) * frequency,
                (settings.offest.y + settings.worldOffset.y + z1) * frequency
            ) * amplitude)
            amplitudeSum += amplitude
            amplitude *= settings.persistance
            frequency *= 2f
        }
        return total / amplitudeSum
    }
}

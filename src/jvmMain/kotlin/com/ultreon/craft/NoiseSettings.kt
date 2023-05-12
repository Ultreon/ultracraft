package com.ultreon.craft

import com.ultreon.craft.util.*


data class NoiseSettings(
    var noiseZoom: Float,
    var octaves: Int,
    var offest: Vector2Int,
    var worldOffset: Vector2Int,
    var persistance: Float,
    var redistributionModifier: Float,
    var exponent: Float
)

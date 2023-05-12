package com.ultreon.craft.world.gen

import com.soywiz.korma.geom.*
import com.ultreon.craft.*
import com.ultreon.craft.util.*


class DomainWarping (
    var noiseDomainX: NoiseSettings,
    var noiseDomainY: NoiseSettings,
    var amplitudeX: Int = 20,
    var amplitudeY: Int = 20
) {
    fun generateDomainNoise(x: Int, z: Int, settings: NoiseSettings): Float {
        val domainOffset: Vector2D = generateDomainOffset(x, z)
        return MyNoise.octaveSimplex((x + domainOffset.x).toFloat(), (z + domainOffset.y).toFloat(), settings)
    }

    fun generateDomainOffset(x: Int, z: Int): Vector2D {
        val noiseX: Float = MyNoise.octaveSimplex(x.toFloat(), z.toFloat(), noiseDomainX) * amplitudeX
        val noiseY: Float = MyNoise.octaveSimplex(x.toFloat(), z.toFloat(), noiseDomainY) * amplitudeY
        return Vector2D(noiseX, noiseY)
    }

    fun generateDomainOffsetInt(x: Int, z: Int): Vector2Int {
        return generateDomainOffset(x, z).roundToInt()
    }
}

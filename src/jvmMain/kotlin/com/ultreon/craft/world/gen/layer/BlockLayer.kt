package com.ultreon.craft.world.gen.layer

import com.soywiz.korge3d.*
import com.ultreon.craft.util.*
import com.ultreon.craft.world.chunk.*


@Korge3DExperimental
abstract class BlockLayer(private val next: BlockLayer? = null) {
    open fun handle(
        chunkData: ChunkData,
        x: Int,
        y: Int,
        z: Int,
        surfaceHeightNoise: Int,
        mapSeedOffset: Vector2Int
    ): Boolean {
        if (tryHandling(chunkData, x, y, z, surfaceHeightNoise, mapSeedOffset)) return true
        return next?.handle(chunkData, x, y, z, surfaceHeightNoise, mapSeedOffset) ?: false
    }

    protected abstract fun tryHandling(
        chunkData: ChunkData,
        x: Int,
        y: Int,
        z: Int,
        surfaceHeightNoise: Int,
        mapSeedOffset: Vector2Int
    ): Boolean
}

package com.ultreon.craft.world.gen.layer

import com.soywiz.korge3d.*
import com.ultreon.craft.util.*
import com.ultreon.craft.world.block.*
import com.ultreon.craft.world.chunk.*


@Korge3DExperimental
class WaterLayer(var waterLevel: Int = 1) : BlockLayer() {
    override fun tryHandling(
        chunkData: ChunkData,
        x: Int,
        y: Int,
        z: Int,
        surfaceHeightNoise: Int,
        mapSeedOffset: Vector2Int
    ): Boolean {
        if (y in (surfaceHeightNoise + 1)..waterLevel) {
            val pos = Vector3Int(x, y, z)
            chunkData.world.setBlock(chunkData, pos, BlockType.Water)
            if (y == surfaceHeightNoise + 1) {
                pos.y = surfaceHeightNoise
                chunkData.world.setBlock(chunkData, pos, BlockType.Sand)
            }
            return true
        }
        return false
    }
}

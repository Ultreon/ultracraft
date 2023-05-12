package com.ultreon.craft.world.gen.layer

import com.soywiz.korge3d.*
import com.ultreon.craft.util.*
import com.ultreon.craft.world.block.*
import com.ultreon.craft.world.chunk.*


@Korge3DExperimental
class StoneLayer : BlockLayer() {
    override fun tryHandling(
        chunkData: ChunkData,
        x: Int,
        y: Int,
        z: Int,
        surfaceHeightNoise: Int,
        mapSeedOffset: Vector2Int
    ): Boolean {
        if (y < surfaceHeightNoise - 3) {
            val pos = Vector3Int(x, y, z)
            chunkData.world.setBlock(chunkData, pos, BlockType.Stone)
            return true
        }
        return false
    }
}

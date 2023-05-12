package com.ultreon.craft.world.gen.layer

import com.soywiz.korge3d.*
import com.ultreon.craft.*
import com.ultreon.craft.init.*
import com.ultreon.craft.util.*
import com.ultreon.craft.world.block.*
import com.ultreon.craft.world.chunk.*
import com.ultreon.craft.world.gen.*


@Korge3DExperimental
class StonePatchLayer(
    var stoneThreshold: Float = 0.5f,
    var stoneNoiseSettings: NoiseSettings = NoiseSettingsInit.stonePatchLayerNoiseSettings(),
    var domainWarping: DomainWarping
) : BlockLayer() {
    override fun tryHandling(
        chunkData: ChunkData,
        x: Int,
        y: Int,
        z: Int,
        surfaceHeightNoise: Int,
        mapSeedOffset: Vector2Int
    ): Boolean {
        if (chunkData.position.y > surfaceHeightNoise) return false

        stoneNoiseSettings.worldOffset = mapSeedOffset
        //float stoneNoise = MyNoise.OctavePerlin(chunkData.worldPosition.x + x, chunkData.worldPosition.z + z, stoneNoiseSettings);
        //float stoneNoise = MyNoise.OctavePerlin(chunkData.worldPosition.x + x, chunkData.worldPosition.z + z, stoneNoiseSettings);
        val stoneNoise: Float = domainWarping.generateDomainNoise(
            chunkData.position.x + x,
            chunkData.position.z + z,
            stoneNoiseSettings
        )

        var endPosition = surfaceHeightNoise
        if (chunkData.position.y < 0) {
            endPosition = chunkData.position.y + chunkData.chunkHeight
        }


        if (stoneNoise > stoneThreshold) {
            for (i in chunkData.position.y..endPosition) {
                val pos = Vector3Int(x, i, z)
                chunkData.world.setBlock(chunkData, pos, BlockType.Stone)
            }
            return true
        }
        return false
    }
}

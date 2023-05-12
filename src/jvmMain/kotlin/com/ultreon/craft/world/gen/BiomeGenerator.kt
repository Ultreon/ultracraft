package com.ultreon.craft.world.gen

import com.soywiz.korge3d.*
import com.ultreon.craft.*
import com.ultreon.craft.util.*
import com.ultreon.craft.world.chunk.*
import com.ultreon.craft.world.gen.layer.*


@Korge3DExperimental
class BiomeGenerator (
    var waterThreshold: Int = 50,
    var biomeNoiseSettings: NoiseSettings,
    var domainWarping: DomainWarping,
    var useDomainWarping: Boolean = true,
    var startLayerHandler: BlockLayer,
    var additionalLayerHandlers: List<BlockLayer>,
//    var treeGenerator: TreeGenerator
) {

    fun processChunkColumn(data: ChunkData, x: Int, z: Int, mapSeedOffset: Vector2Int): ChunkData {
        biomeNoiseSettings.worldOffset = mapSeedOffset;
        val groundPosition = getSurfaceHeightNoise(data.position.x + x, data.position.z + z, data.chunkHeight);

        for (y in data.position.y..data.position.y + data.chunkHeight) {
            startLayerHandler.handle(data, x, y, z, groundPosition, mapSeedOffset);
        }

        for (layer in additionalLayerHandlers) {
            layer.handle(data, x, data.position.y, z, groundPosition, mapSeedOffset);
        }

        return data;
    }

//    fun getTreeData(data: ChunkData?, mapSeedOffset: Vector2Int?): TreeData? {
//        return if (null.also { treeGenerator = it }!!) TreeData() else treeGenerator.GenerateTreeData(
//            data,
//            mapSeedOffset
//        )
//    }


    private fun getSurfaceHeightNoise(x: Int, z: Int, chunkHeight: Int): Int {
        var terrainHeight: Float = if (!useDomainWarping) {
            MyNoise.octaveSimplex(x.toFloat(), z.toFloat(), biomeNoiseSettings)
        } else {
            domainWarping.generateDomainNoise(x, z, biomeNoiseSettings)
        }
        terrainHeight = MyNoise.redistribution(terrainHeight, biomeNoiseSettings)
        return MyNoise.remapValueInt(terrainHeight, 0F, chunkHeight.toFloat())
    }
}

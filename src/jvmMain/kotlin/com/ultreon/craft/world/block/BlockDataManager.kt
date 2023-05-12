@file:Suppress("unused")

package com.ultreon.craft.world.block

import kotlin.native.concurrent.*

class BlockDataManager(
    var textureData: BlockDataSO
) {
    fun init() {
        for (item in textureData.textureDataList) {
            if (item.blockType !in blockTextureDataDictionary) {
                blockTextureDataDictionary += Pair(item.blockType, item)
            }
        }
        tileSizeX = textureData.textureSizeX
        tileSizeY = textureData.textureSizeY
    }

    companion object {
        var textureOffset: Float = 0.001f
        var tileSizeX: Float = 0f
        var tileSizeY: Float = 0f
        var blockTextureDataDictionary: MutableMap<BlockType, TextureData> = mutableMapOf()
    }
}

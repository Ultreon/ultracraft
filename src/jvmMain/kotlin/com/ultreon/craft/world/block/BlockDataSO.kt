package com.ultreon.craft.world.block

import com.soywiz.korma.geom.*

data class BlockDataSO (
    var textureSizeX: Float = 0f,
    var textureSizeY: Float = 0f,
    var textureDataList: List<TextureData>
)

data class TextureData (
    var blockType: BlockType,
    var up: PointInt,
    var down: PointInt,
    var side: PointInt,
    var isSolid: Boolean = true,
    var generatesCollider: Boolean = true
)

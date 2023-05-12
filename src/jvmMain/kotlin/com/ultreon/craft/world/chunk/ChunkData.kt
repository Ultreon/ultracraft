package com.ultreon.craft.world.chunk

import com.soywiz.korge3d.Korge3DExperimental
import com.ultreon.craft.world.block.BlockType
import com.ultreon.craft.UltreonCraft
import com.ultreon.craft.util.Vector3Int
import com.ultreon.craft.world.World
import com.ultreon.craft.world.WorldConfig

@Korge3DExperimental
class ChunkData(
    var chunkSize: Int = WorldConfig.chunkSize,
    var chunkHeight: Int = WorldConfig.chunkHeight,
    var world: World = UltreonCraft.instance.world!!,
    var position: Vector3Int
) {
    var blocks: Array<BlockType?> = arrayOfNulls(chunkSize * chunkHeight * chunkSize)
    var modifiedByThePlayer: Boolean = false
//    var treeData: TreeData? = null
}

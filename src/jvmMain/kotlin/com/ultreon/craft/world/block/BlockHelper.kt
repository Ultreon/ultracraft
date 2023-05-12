package com.ultreon.craft.world.block

import com.soywiz.korge3d.*
import com.soywiz.korma.geom.*
import com.ultreon.craft.*
import com.ultreon.craft.util.*
import com.ultreon.craft.util.Direction.*
import com.ultreon.craft.world.chunk.*
import com.ultreon.craft.UltreonCraft.Companion.instance as game


@Korge3DExperimental
object BlockHelper {
    fun getFaceDataIn(
        direction: Direction?,
        chunk: ChunkData?,
        x: Int,
        y: Int,
        z: Int,
        meshData: MeshData,
        blockType: BlockType?
    ): MeshData {
        getFaceVertices(direction, x, y, z, meshData, blockType)
        meshData.addIndices(BlockDataManager.blockTextureDataDictionary[blockType]!!.generatesCollider)
        return meshData
    }

    private val directions = arrayOf(
        BACKWARDS,
        DOWN,
        FORWARDS,
        LEFT,
        RIGHT,
        UP
    )

    fun getMeshData(chunk: ChunkData, x: Int, y: Int, z: Int, meshData: MeshData, blockType: BlockType): MeshData {
        var result = meshData
        if (blockType == BlockType.Air || blockType == BlockType.Nothing)
            return result

        for (direction in directions)
        {
            val neighbourBlockCoordinates = Vector3Int(x, y, z) + direction.vector
            val neighbourBlockType = game.world!!.getBlockFromChunkCoordinates(chunk, neighbourBlockCoordinates)

            if (neighbourBlockType != BlockType.Nothing && !BlockDataManager.blockTextureDataDictionary[neighbourBlockType]!!.isSolid)
            {

                if (blockType == BlockType.Water)
                {
                    if (neighbourBlockType == BlockType.Air)
                        result.waterMesh = getFaceDataIn(direction, chunk, x, y, z, result.waterMesh!!, blockType)
                }
                else
                {
                    result = getFaceDataIn(direction, chunk, x, y, z, result, blockType)
                }
            }
        }

        return result
    }

    fun getFaceVertices(direction: Direction?, x: Int, y: Int, z: Int, meshData: MeshData, blockType: BlockType?) {
        val generatesCollider: Boolean = BlockDataManager.blockTextureDataDictionary[blockType]?.generatesCollider ?: false
        when (direction) {
            BACKWARDS -> {
                meshData.addVertex(Vector3(x - 0.5f, y - 0.5f, z - 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x - 0.5f, y + 0.5f, z - 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x + 0.5f, y + 0.5f, z - 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x + 0.5f, y - 0.5f, z - 0.5f), generatesCollider)
            }

            FORWARDS -> {
                meshData.addVertex(Vector3(x + 0.5f, y - 0.5f, z + 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x + 0.5f, y + 0.5f, z + 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x - 0.5f, y + 0.5f, z + 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x - 0.5f, y - 0.5f, z + 0.5f), generatesCollider)
            }

            LEFT -> {
                meshData.addVertex(Vector3(x - 0.5f, y - 0.5f, z + 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x - 0.5f, y + 0.5f, z + 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x - 0.5f, y + 0.5f, z - 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x - 0.5f, y - 0.5f, z - 0.5f), generatesCollider)
            }

            RIGHT -> {
                meshData.addVertex(Vector3(x + 0.5f, y - 0.5f, z - 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x + 0.5f, y + 0.5f, z - 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x + 0.5f, y + 0.5f, z + 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x + 0.5f, y - 0.5f, z + 0.5f), generatesCollider)
            }

            DOWN -> {
                meshData.addVertex(Vector3(x - 0.5f, y - 0.5f, z - 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x + 0.5f, y - 0.5f, z - 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x + 0.5f, y - 0.5f, z + 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x - 0.5f, y - 0.5f, z + 0.5f), generatesCollider)
            }

            UP -> {
                meshData.addVertex(Vector3(x - 0.5f, y + 0.5f, z + 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x + 0.5f, y + 0.5f, z + 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x + 0.5f, y + 0.5f, z - 0.5f), generatesCollider)
                meshData.addVertex(Vector3(x - 0.5f, y + 0.5f, z - 0.5f), generatesCollider)
            }

            else -> {}
        }
    }

}

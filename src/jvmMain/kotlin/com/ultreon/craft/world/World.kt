package com.ultreon.craft.world

import com.soywiz.kds.*
import com.soywiz.korge3d.*
import com.ultreon.craft.*
import com.ultreon.craft.util.*
import com.ultreon.craft.world.block.*
import com.ultreon.craft.world.chunk.*
import com.ultreon.craft.world.gen.*
import kotlin.math.*


@Korge3DExperimental
class World {
    var worldData: WorldData

    var mapSizeInChunks = 6
    var chunkSize = 16
    var chunkHeight = 100
    var chunkDrawingRange = 8

//    var chunkPrefab: GameObject? = null
    var worldRenderer: WorldRenderer? = null

    var terrainGenerator: TerrainGenerator? = null
    var mapSeedOffset: Vector2Int? = null

    init {
        worldData = WorldData(
            chunkHeight = this.chunkHeight,
            chunkSize = this.chunkSize,
            chunkDataMap = CopyOnWriteFrozenMap(),
            chunks = mutableMapOf()
        )
    }

    fun loopThroughTheBlocks(chunkData: ChunkData, actionToPerform: (Int, Int, Int) -> Unit) {
        for (index in 0 until chunkData.blocks.size) {
            val position: Vector3Int = getPostitionFromIndex(chunkData, index)
            actionToPerform(position.x, position.y, position.z)
        }
    }

    private fun getPostitionFromIndex(chunkData: ChunkData, index: Int): Vector3Int {
        val x = index % chunkData.chunkSize
        val y = index / chunkData.chunkSize % chunkData.chunkHeight
        val z = index / (chunkData.chunkSize * chunkData.chunkHeight)
        return Vector3Int(x, y, z)
    }

    //in chunk coordinate system
    private fun inRange(chunkData: ChunkData, axisCoordinate: Int): Boolean {
        return !(axisCoordinate < 0 || axisCoordinate >= chunkData.chunkSize)
    }

    //in chunk coordinate system
    private fun inRangeHeight(chunkData: ChunkData, ycoordinate: Int): Boolean {
        return !(ycoordinate < 0 || ycoordinate >= chunkData.chunkHeight)
    }
    fun getBlockFromChunkCoordinates(chunkData: ChunkData, chunkCoordinates: Vector3Int): BlockType {
        return getBlockFromChunkCoordinates(chunkData, chunkCoordinates.x, chunkCoordinates.y, chunkCoordinates.z)!!
    }

    fun getBlockFromChunkCoordinates(chunkData: ChunkData, x: Int, y: Int, z: Int): BlockType? {
        if (inRange(chunkData, x) && inRangeHeight(chunkData, y) && inRange(chunkData, z)) {
            val index: Int = getIndexFromPosition(chunkData, x, y, z)
            return chunkData.blocks[index]
        }
        return chunkData.world.getBlockFromChunkCoordinates(
            chunkData,
            chunkData.position.x + x,
            chunkData.position.y + y,
            chunkData.position.z + z
        )
    }

    fun setBlock(chunkData: ChunkData, localPosition: Vector3Int, block: BlockType) {
        if (inRange(chunkData, localPosition.x) && inRangeHeight(chunkData, localPosition.y) && inRange(
                chunkData,
                localPosition.z
            )
        ) {
            val index: Int = getIndexFromPosition(chunkData, localPosition.x, localPosition.y, localPosition.z)
            chunkData.blocks[index] = block
        } else {
            setBlock(localPosition, block)
        }
    }

    fun setBlock(pos: Vector3Int, blockType: BlockType) {
        val chunkData: ChunkData? = getChunkData(pos)
        if (chunkData != null)
        {
            val localPosition: Vector3Int = getBlockInChunkCoordinates(chunkData, pos)
            setBlock(chunkData, localPosition, blockType)
        }
    }

    fun getChunkData(pos: Vector3Int): ChunkData? {
        val chunkPosition: Vector3Int = chunkPositionFromBlockCoords(pos)
        return worldData.chunkDataMap[chunkPosition]
    }

    private fun getIndexFromPosition(chunkData: ChunkData, x: Int, y: Int, z: Int): Int {
        return x + chunkData.chunkSize * y + chunkData.chunkSize * chunkData.chunkHeight * z
    }

    fun getBlockInChunkCoordinates(chunkData: ChunkData, pos: Vector3Int): Vector3Int {
        return Vector3Int(
            x = pos.x - chunkData.position.x,
            y = pos.y - chunkData.position.y,
            z = pos.z - chunkData.position.z
        )
    }

    fun getChunkMeshData(chunkData: ChunkData): MeshData {
        var meshData = MeshData(true)

        loopThroughTheBlocks(chunkData) { x, y, z ->
            meshData = BlockHelper.getMeshData(
                chunkData,
                x,
                y,
                z,
                meshData,
                chunkData.blocks[getIndexFromPosition(chunkData, x, y, z)]!!
            )
        }


        return meshData
    }

    fun chunkPositionFromBlockCoords(position: Vector3Int): Vector3Int {
        return Vector3Int(
            x = floor(position.x / chunkSize.toFloat()).toInt() * chunkSize,
            y = floor(position.y / chunkHeight.toFloat()).toInt() * chunkHeight,
            z = floor(position.z / chunkSize.toFloat()).toInt() * chunkSize
        )
    }

    fun isOnEdge(chunkData: ChunkData, worldPosition: Vector3Int): Boolean {
        val chunkPosition: Vector3Int = getBlockInChunkCoordinates(chunkData, worldPosition)
        return chunkPosition.x == 0 || chunkPosition.x == chunkData.chunkSize - 1 || chunkPosition.y == 0 || chunkPosition.y == chunkData.chunkHeight - 1 || chunkPosition.z == 0 || chunkPosition.z == chunkData.chunkSize - 1
    }
    internal fun getChunkPositionsAroundPlayer(playerPosition: Vector3Int): List<Vector3Int> {
        val startX: Int = playerPosition.x - (chunkDrawingRange) * chunkSize
        val startZ: Int = playerPosition.z - (chunkDrawingRange) * chunkSize
        val endX: Int = playerPosition.x + (chunkDrawingRange) * chunkSize
        val endZ: Int = playerPosition.z + (chunkDrawingRange) * chunkSize

        val chunkPositionsToCreate: MutableList<Vector3Int> = mutableListOf()
        for (x in startX..endX) {
            for (z in startZ..endZ) {
                var chunkPos: Vector3Int = chunkPositionFromBlockCoords(Vector3Int(x, 0, z))
                chunkPositionsToCreate += chunkPos
                if (x >= playerPosition.x - chunkSize
                    && x <= playerPosition.x + chunkSize
                    && z >= playerPosition.z - chunkSize
                    && z <= playerPosition.z + chunkSize) {
                    for (y in -chunkHeight downTo playerPosition.y - chunkHeight * 2) {
                        chunkPos = chunkPositionFromBlockCoords(Vector3Int(x, y, z))
                        chunkPositionsToCreate += chunkPos
                    }
                }
            }
        }

        return chunkPositionsToCreate
    }

    internal fun removeChunkData(world: World, pos: Vector3Int) {
        world.worldData.chunkDataMap.remove(pos)
    }

    class WorldGenData {
        var chunkPositionsToCreate: List<Vector3Int>? = null
        var chunkDataPositionsToCreate: List<Vector3Int>? = null
        var chunkPositionsToRemove: List<Vector3Int>? = null
        var chunkDataToRemove: List<Vector3Int>? = null
        var chunkPositionsToUpdate: List<Vector3Int>? = null
    }
}

package com.ultreon.craft.world

import com.soywiz.kds.Queue
import com.soywiz.korge3d.Korge3DExperimental
import com.ultreon.craft.world.chunk.Chunk
import com.ultreon.craft.world.chunk.ChunkData
import com.ultreon.craft.MeshData
import com.ultreon.craft.util.Vector3Int

@Korge3DExperimental
class WorldRenderer {
    val chunkPool: Queue<Chunk> = Queue()

    fun clear(worldData: WorldData) {
        for (value in worldData.chunks.values) {
            value.destroy()
        }
        worldData.chunks.clear()
        chunkPool.clear()
    }

    fun createChunk(worldData: WorldData, position: Vector3Int, meshData: MeshData) : Chunk {
        val newChunk: Chunk = if (chunkPool.isNotEmpty()) {
            chunkPool.dequeue().also {
                it.position = position
            }
        } else {
            Chunk(worldData.chunkDataMap[position] ?: ChunkData(position = position))
        }

        newChunk.updateChunk(meshData)
        newChunk.active = true

        return newChunk
    }

    fun removeChunk(chunk: Chunk) {
        chunk.active = false
        chunkPool.enqueue(chunk)
    }
}

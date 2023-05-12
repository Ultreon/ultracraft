package com.ultreon.craft.world.chunk

import com.soywiz.korge3d.Korge3DExperimental
import com.soywiz.korge3d.Mesh3D
import com.ultreon.craft.MeshData
import com.ultreon.craft.UltreonCraft
import com.ultreon.craft.util.Vector3Int

@Korge3DExperimental
class Chunk(var chunkData: ChunkData, var position: Vector3Int = chunkData.position) {
    var active: Boolean = false
    var mesh: Mesh3D? = null
    var modified: Boolean
        get() {
            return chunkData.modifiedByThePlayer
        }
        set(value) {
            chunkData.modifiedByThePlayer = value
        }

    fun renderMesh(meshData: MeshData) {
        mesh = meshData.build()
    }

    fun updateChunk() {
        renderMesh(UltreonCraft.instance.world!!.getChunkMeshData(chunkData))
    }

    fun updateChunk(meshData: MeshData) {
        renderMesh(meshData)
    }

    fun destroy() {

    }

    companion object {
        operator fun invoke(position: Vector3Int): Chunk {
            return Chunk(ChunkData(position = position))
        }
    }
}

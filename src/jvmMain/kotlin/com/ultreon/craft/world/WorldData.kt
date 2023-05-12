package com.ultreon.craft.world

import com.soywiz.kds.*
import com.soywiz.korge3d.Korge3DExperimental
import com.ultreon.craft.world.chunk.Chunk
import com.ultreon.craft.world.chunk.ChunkData
import com.ultreon.craft.util.Vector3Int

@Korge3DExperimental
data class WorldData(
    var chunkDataMap: CopyOnWriteFrozenMap<Vector3Int, ChunkData>,
    var chunks: MutableMap<Vector3Int, Chunk>,
    var chunkSize: Int = 0,
    var chunkHeight: Int = 0
)

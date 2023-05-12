package com.ultreon.craft.world.gen

import com.soywiz.korge3d.*
import com.ultreon.craft.util.*
import com.ultreon.craft.world.chunk.*


@Korge3DExperimental
class TerrainGenerator(var biomeGenerator: BiomeGenerator) {
    fun generateChunkData(data: ChunkData, mapSeedOffset: Vector2Int): ChunkData? {
        //TreeData treeData = biomeGenerator.GetTreeData();
        //data.treeData = treeData;
        var data = data
        for (x in 0 until data.chunkSize) {
            for (z in 0 until data.chunkSize) {
                data = biomeGenerator.processChunkColumn(data, x, z, mapSeedOffset)
            }
        }
        return data
    }
}

package com.ultreon.craft

import com.soywiz.kmem.*
import com.soywiz.korge3d.*
import com.soywiz.korma.geom.*

@Korge3DExperimental
class MeshData(isMainMesh: Boolean = true) {
    private lateinit var mesh: Mesh3D
    private lateinit var colliderMesh: Mesh3D
    private val builder = MeshBuilder3D()
    private val colliderBuilder = MeshBuilder3D()

    var waterMesh: MeshData? = if (isMainMesh) MeshData(false) else null

    fun addVertex(vertex: Vector3, vertexGeneratesCollider: Boolean) {
        builder.addVertex(vertex.x, vertex.y, vertex.z)

        if (vertexGeneratesCollider) {
            colliderBuilder.addVertex(vertex.x, vertex.y, vertex.z)
        }
    }

    fun addIndices(hasCollider: Boolean) {
        builder.addIndices(3, 2, 1, 3, 1, 0);

        if (hasCollider) {
            colliderBuilder.addIndices(3, 2, 1, 3, 1, 0);
        }
    }

    fun build(): Mesh3D {
        return if (this::mesh.isInitialized) mesh else builder.build().also { mesh = it}
    }

    fun buildCollider(): Mesh3D {
        return if (this::colliderMesh.isInitialized) colliderMesh else colliderBuilder.build().also { colliderMesh = it}
    }
}

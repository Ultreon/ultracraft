package com.ultreon.craft.client.world;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.debug.ValueTracker;
import com.ultreon.craft.world.Chunk;

public class ChunkMesh implements Pool.Poolable {
    public final Renderable renderable;
    public final MeshPart meshPart;
    public final Matrix4 transform;
    public Chunk chunk;

    public static long getMeshesDisposed() {
        return ValueTracker.getMeshDisposes();
    }

    public ChunkMesh() {
        this(new Renderable());
    }

    public ChunkMesh(Mesh mesh) {
        this(new Renderable());
        this.meshPart.mesh = mesh;
    }

    public ChunkMesh(Mesh mesh, Material material) {
        this(new Renderable());
        this.meshPart.mesh = mesh;
        this.renderable.material = material;
    }

    public ChunkMesh(Renderable renderable) {
        this.renderable = renderable;
        this.meshPart = renderable.meshPart;
        this.transform = renderable.worldTransform;
    }

    @Override
    public void reset() {
        ValueTracker.setMeshDisposes(ValueTracker.getMeshDisposes() + 1);
        if (this.meshPart.mesh != null) {
            ValueTracker.setVertexCount(ValueTracker.getVertexCount() - this.meshPart.mesh.getMaxVertices());
            this.meshPart.mesh.dispose();

            this.meshPart.mesh = null;
        }
        this.meshPart.id = null;
        this.meshPart.center.setZero();
        this.meshPart.halfExtents.setZero();
        this.meshPart.offset = 0;
        this.meshPart.primitiveType = 0;
        this.meshPart.size = 0;
        this.meshPart.radius = -1;
        this.transform.idt();
        this.renderable.material = null;
        this.renderable.environment = null;
        this.renderable.bones = null;
        this.renderable.shader = null;
        this.renderable.userData = null;
    }
}

package com.ultreon.craft.render.world;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.world.Section;

public class ChunkMesh implements Pool.Poolable {
    public final Renderable renderable;
    public final MeshPart meshPart;
    public final Matrix4 transform;
    public Section section;

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
        this.meshPart.mesh.dispose();
        this.meshPart.mesh = null;
        this.meshPart.id = null;
        this.meshPart.center.setZero();
        this.meshPart.halfExtents.setZero();
        this.meshPart.offset = 0;
        this.meshPart.primitiveType = 0;
        this.meshPart.size = 0;
        this.meshPart.radius = -1;
        this.meshPart.update();
        this.transform.idt();
        this.renderable.material = null;
        this.renderable.environment = null;
        this.renderable.bones = null;
        this.renderable.shader = null;
        this.renderable.userData = null;
    }
}

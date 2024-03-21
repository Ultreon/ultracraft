package com.ultreon.craft.client.model.blockbench;

import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec3f;

import java.util.Map;
import java.util.UUID;

public abstract sealed class BBModelElement implements BBModelNode permits BBCubeModelElement, BBMeshModelElement {
    BBModelNode parent;
    private Matrix4 rotationMatrix;

    public abstract String name();

    public abstract Color color();

    public abstract Vec3f origin();

    public abstract boolean locked();

    public abstract String renderOrder();

    public abstract boolean allowMirrorModeling();

    public abstract UUID uuid();

    public abstract Vec3f rotation();

    public abstract Node write(ModelBuilder groupBuilder, Map<UUID, ModelBuilder> subNodes, Map<Integer, BBTexture> texture2texture, BlockBenchModelImporter modelData, Vec2f resolution);

    public Matrix4 rotationMatrix() {
        if (rotationMatrix == null) {
            this.rotationMatrix = new Matrix4();
            this.rotationMatrix.rotate(Vector3.X, this.rotation().x);
            this.rotationMatrix.rotate(Vector3.Y, this.rotation().y + 90);
            this.rotationMatrix.rotate(Vector3.Z, this.rotation().z);
        }
        return rotationMatrix;
    }

    @Override
    public BBModelNode parent() {
        return parent;
    }
}

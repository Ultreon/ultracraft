package com.ultreon.craft.client.model.blockbench;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.ultreon.libs.commons.v0.vector.Vec2f;

import java.util.Map;

public abstract sealed class BBModelElement permits BBCubeModelElement, BBMeshModelElement {
    public abstract void write(ModelBuilder model, Map<Integer, MeshBuilder> texture2builder, BlockBenchModelImporter modelData, Vec2f resolution);
}

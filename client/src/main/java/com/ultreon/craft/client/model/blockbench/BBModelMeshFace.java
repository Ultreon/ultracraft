package com.ultreon.craft.client.model.blockbench;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec4f;

import java.util.List;
import java.util.Map;

public record BBModelMeshFace(Map<String, Vec2f> uvs, List<BBModelVertex> vertices, int texture) {
    public void write(ModelBuilder builder, Map<Integer, MeshBuilder> texture2builder, Vec2f resolution) {
        MeshBuilder meshBuilder = texture2builder.get(texture);
        meshBuilder.setUVRange(0, 0, resolution.x, resolution.y); // FIXME is this right?
        for (BBModelVertex vertex : vertices) {
            vertex.write(meshBuilder, resolution);
        }
    }
}

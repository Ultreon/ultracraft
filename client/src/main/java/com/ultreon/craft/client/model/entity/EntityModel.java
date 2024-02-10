package com.ultreon.craft.client.model.entity;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.client.render.EntityTextures;
import com.ultreon.craft.entity.Entity;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;

public abstract class EntityModel<T extends Entity> {
    public final Model finish(EntityTextures textures) {
        ModelBuilder builder = new ModelBuilder();
        builder.begin();
        this.build(builder, textures);
        return builder.end();
    }

    protected abstract void build(ModelBuilder builder, EntityTextures textures);

    @CanIgnoreReturnValue
    protected BoxBuilder box(int x, int y, int z, int width, int height, int depth) {
        MeshBuilder builder = new MeshBuilder();
        builder.begin(new VertexAttributes(VertexAttribute.Position(), VertexAttribute.ColorPacked(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)), GL_TRIANGLES);
        builder.setColor(1, 1, 1, 1);
        return new BoxBuilder(builder, x, y, z, width, height, depth);
    }
}

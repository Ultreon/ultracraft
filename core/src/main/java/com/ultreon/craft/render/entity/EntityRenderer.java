package com.ultreon.craft.render.entity;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.util.EntitySize;
import com.ultreon.craft.util.LightUtils;
import com.ultreon.libs.commons.v0.vector.Vec3f;

import java.util.HashMap;
import java.util.Map;

public abstract class EntityRenderer<T extends Entity> {
    private final EntityModelContext context;
    private final Map<EntityType<?>, Mesh> hitBoxes = new HashMap<>();

    public EntityRenderer(EntityModelContext context) {
        this.context = context;
    }

    public abstract void build();

    public void getRenderables(Array<Renderable> renderables, T entity, Pool<Renderable> pool, Vec3f offsetPos, int light) {
        this.setAngles(entity);

        System.out.println("offsetPos = " + offsetPos);

        Map<String, Mesh> meshes = this.context.meshes;

        float brightness = LightUtils.getBrightness(light);
        Material material = this.getMaterial(entity);
        material.set(ColorAttribute.createDiffuse(new Color(brightness, brightness, brightness, 1.0F)));

        meshes.forEach((name, mesh) -> {
            Renderable piece = pool.obtain();
            piece.meshPart.mesh = mesh;
            piece.meshPart.size = mesh.getNumVertices();
            piece.meshPart.offset = 0;
            piece.meshPart.primitiveType = GL20.GL_TRIANGLES;
            piece.material = material;
            piece.environment = UltreonCraft.get().env;
            piece.worldTransform.set(this.context.getOffset(name).cpy().add(offsetPos.x, offsetPos.y, offsetPos.z), this.context.getRotation(name));

            renderables.add(piece);
        });

        if (UltreonCraft.get().showHitBoxes) {
            Renderable piece = pool.obtain();
            EntityType<? extends Entity> type = entity.getType();

            Mesh mesh;
            if (this.hitBoxes.containsKey(type)) {
                MeshBuilder meshBuilder = new MeshBuilder();
                meshBuilder.begin(new VertexAttributes(VertexAttribute.Position(), VertexAttribute.ColorPacked()), GL20.GL_LINES);
                meshBuilder.setColor(Color.WHITE);

                EntitySize size = entity.getSize();
                float x1 = 0 - size.width() / 2;
                float y1 = 0;
                float z1 = 0 - size.width() / 2;
                float x2 = 0 + size.width() / 2;
                float y2 = 0 + size.height();
                float z2 = 0 + size.width() / 2;

                BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(x1, y1, z1), new Vector3(x2, y2, z2)));
                mesh = meshBuilder.end();
            } else {
                mesh = this.hitBoxes.get(type);
            }

            piece.meshPart.mesh = mesh;
            piece.meshPart.size = mesh.getNumVertices();
            piece.meshPart.offset = 0;
            piece.meshPart.primitiveType = GL20.GL_LINES;
            piece.material = material;
            piece.environment = UltreonCraft.get().env;
            piece.worldTransform.setToTranslation(offsetPos.x, offsetPos.y, offsetPos.z);

            renderables.add(piece);
        }
    }

    protected void setAngles(T entity) {

    }

    protected abstract Material getMaterial(T entity);

    public EntityModelContext getContext() {
        return this.context;
    }
}

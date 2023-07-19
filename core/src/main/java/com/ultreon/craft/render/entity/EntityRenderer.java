package com.ultreon.craft.render.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.util.LightUtils;
import com.ultreon.libs.commons.v0.vector.Vec3f;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class EntityRenderer<T extends Entity> {
    private final EntityModelContext context;

    public EntityRenderer(EntityModelContext context) {
        this.context = context;
    }

    public abstract void build();

    public void getRenderables(Array<Renderable> renderables, T entity, Pool<Renderable> pool, Vec3f offsetPos, int light) {
        this.setAngles(entity);

        Map<String, Mesh> meshes = this.context.meshes;

        float brightness = LightUtils.getBrightness(light);
        Material material = this.getMaterial(entity);
        material.set(ColorAttribute.createDiffuse(new Color(brightness, brightness, brightness, 1.0F)));

        meshes.forEach((name, mesh) -> {
            Renderable obtain = pool.obtain();
            obtain.meshPart.mesh = mesh;
            obtain.meshPart.size = mesh.getNumVertices();
            obtain.meshPart.offset = 0;
            obtain.material = material;
            obtain.environment = UltreonCraft.get().env;
            obtain.worldTransform.set(this.context.getOffset(name).cpy().add(offsetPos.x, offsetPos.y, offsetPos.z), this.context.getRotation(name));
        });
    }

    protected void setAngles(T entity) {

    }

    protected abstract Material getMaterial(T entity);

    public EntityModelContext getContext() {
        return this.context;
    }
}

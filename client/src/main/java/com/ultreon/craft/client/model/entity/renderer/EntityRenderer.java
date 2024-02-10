package com.ultreon.craft.client.model.entity.renderer;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.init.Shaders;
import com.ultreon.craft.client.model.EntityModelInstance;
import com.ultreon.craft.client.model.WorldRenderContext;
import com.ultreon.craft.client.render.EntityTextures;
import com.ultreon.craft.entity.Entity;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import org.jetbrains.annotations.Nullable;

public abstract class EntityRenderer<E extends Entity> {
    protected static Vec3d tmp0 = new Vec3d();
    protected static Vec3d tmp1 = new Vec3d();
    protected static Vec3d tmp2 = new Vec3d();
    protected static Vec3f tmp0f = new Vec3f();
    protected static Vec3f tmp1f = new Vec3f();
    protected static Vec3f tmp2f = new Vec3f();

    protected UltracraftClient client = UltracraftClient.get();
    protected Matrix4 tmp = new Matrix4();

    protected EntityRenderer() {

    }

    public void render(EntityModelInstance<E> instance, WorldRenderContext<E> context) {
        if (instance.getModel() == null)
            throw new IllegalStateException("Cannot render entity " + instance.getEntity().getType().getId() + " without model");

        if (instance.getModel().nodes.size == 0)
            throw new IllegalStateException("Cannot render entity " + instance.getEntity().getType().getId() + " without nodes");

        if (instance.getModel().materials.size == 0)
            throw new IllegalStateException("Cannot render entity " + instance.getEntity().getType().getId() + " without materials");

        instance.getModel().userData = Shaders.MODEL_VIEW;
        instance.render(context);
    }

    public abstract void animate(EntityModelInstance<E> instance, WorldRenderContext<E> context);

    @Nullable
    public abstract ModelInstance createModel(E entity);

    public abstract EntityTextures getTextures();
}

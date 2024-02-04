package com.ultreon.craft.client.model.entity.renderer;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.model.entity.EntityModel;
import com.ultreon.craft.client.render.EntityTextures;
import com.ultreon.craft.entity.Entity;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3f;

public abstract class EntityRenderer<M extends EntityModel<?>, E extends Entity> {
    protected static Vec3d tmp0 = new Vec3d();
    protected static Vec3d tmp1 = new Vec3d();
    protected static Vec3d tmp2 = new Vec3d();
    protected static Vec3f tmp0f = new Vec3f();
    protected static Vec3f tmp1f = new Vec3f();
    protected static Vec3f tmp2f = new Vec3f();

    private final M model;
    protected UltracraftClient client = UltracraftClient.get();
    protected Matrix4 tmp = new Matrix4();

    protected EntityRenderer(M model) {
        this.model = model;
    }

    public M getModel() {
        return this.model;
    }

    public void render(ModelInstance instance, Array<Renderable> output, Pool<Renderable> renderablePool) {
        instance.getRenderables(output, renderablePool);
    }

    public abstract void animate(ModelInstance instance, E entity);

    public ModelInstance createInstance(Model model) {
        return new ModelInstance(model);
    }

    public abstract EntityTextures getTextures();
}

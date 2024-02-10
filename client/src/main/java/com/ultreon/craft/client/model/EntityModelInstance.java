package com.ultreon.craft.client.model;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.texture.TextureManager;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.util.ElementID;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3f;

public class EntityModelInstance<T extends Entity> {
    private static final Vec3d TMP = new Vec3d();
    private static final Vector3 TMP_1 = new Vector3();
    private final ModelInstance model;
    private final T entity;
    private final Matrix4 transform = new Matrix4();

    public EntityModelInstance(ModelInstance model, T entity) {
        this.model = model;
        this.entity = entity;

        transform.setToScaling(1, 1, 1);
    }

    public ModelInstance getModel() {
        return model;
    }

    public void translate(double x, double y, double z) {
        this.transform.translate((float) x, (float) y, (float) z);
    }

    public void scale(double x, double y, double z) {
        this.transform.scale((float) x, (float) y, (float) z);
    }

    public void rotateX(double angle) {
        this.transform.rotate(Vector3.X, (float) angle);
    }

    public void rotateY(double angle) {
        this.transform.rotate(Vector3.Y, (float) angle);
    }

    public void rotateZ(double angle) {
        this.transform.rotate(Vector3.Z, (float) angle);
    }

    public Matrix4 getTransform() {
        return transform;
    }

    public void setTranslation(double x, double y, double z) {
        this.transform.setTranslation((float) x, (float) y, (float) z);
    }

    public void translate(Vec3d translation) {
        translate(translation.x, translation.y, translation.z);
    }

    public void translate(Vec3f translation) {
        this.transform.translate(translation.x, translation.y, translation.z);
    }

    public void translate(int x, int y, int z) {
        this.transform.translate(x, y, z);
    }

    public void translate(float x, float y, float z) {
        this.transform.translate(x, y, z);
    }

    public void translate(Vector3 translation) {
        this.transform.translate(translation);
    }

    public Vector3 getTranslation(Vector3 out) {
        return this.transform.getTranslation(out);
    }

    public void render(WorldRenderContext<? super T> context) {
        WorldRenderContextImpl<? super T> ctx = (WorldRenderContextImpl<? super T>) context;
        model.transform.set(transform);

        Vec3f translation = ctx.relative(entity.getPosition(), TMP).f();
        Vector3 tmp = model.transform.getTranslation(TMP_1).add(translation.x, translation.y, translation.z);
        model.transform.setTranslation(tmp);

        model.calculateTransforms();

        context.render(model);
    }

    public void setTextures(ElementID textureLocation) {
        TextureManager textureManager = UltracraftClient.get().getTextureManager();
        TextureAttribute diffuseTexture = TextureAttribute.createDiffuse(textureManager.getTexture(textureLocation));
        model.getMaterial("player.png").set(diffuseTexture);
    }

    public T getEntity() {
        return entity;
    }

    public void setTranslation(Vec3d position) {
        setTranslation(position.x, position.y, position.z);
    }

    public void scale(Vec3d scale) {
        scale(scale.x, scale.y, scale.z);
    }

    public void scale(Vec3f scale) {
        scale(scale.x, scale.y, scale.z);
    }

    public void scale(float x, float y, float z) {
        this.transform.scale(x, y, z);
    }

    public void scale(Vector3 scale) {
        this.transform.scale(scale.x, scale.y, scale.z);
    }

    public void scale(int x, int y, int z) {
        this.transform.scale(x, y, z);
    }

    public void scale(int scale) {
        scale(scale, scale, scale);
    }

    public Node getNode(String name) {
        return model.getNode(name);
    }
}

package com.ultreon.craft.client.model.entity.renderer;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.client.model.entity.EntityModel;
import com.ultreon.craft.client.render.EntityTextures;
import com.ultreon.craft.entity.DroppedItem;
import org.jetbrains.annotations.NotNull;

public class DroppedItemRenderer extends EntityRenderer<@NotNull DroppedItem> {
    protected DroppedItemRenderer() {
        super();
    }

    @Override
    public void animate(ModelInstance instance, DroppedItem entity) {
        int age = entity.getAge();
        int rotation = age * 10 % 360;
        float translation = age * 0.125f % 0.25f;
        if (translation > 0.125f) {
            translation = 0.25f - translation;
        }

        instance.transform.idt().rotate(Vector3.Y, rotation).translate(0, translation, 0);
    }

    @Override
    public void render(ModelInstance instance, Array<Renderable> output, Pool<Renderable> renderablePool) {
        super.render(instance, output, renderablePool);
    }

    @Override
    public ModelInstance createInstance(@NotNull DroppedItem entity) {
        if (entity.getStack().isEmpty()) return null;

        return client.itemRenderer.createModelInstance(entity.getStack());
    }

    @Override
    public EntityTextures getTextures() {
        return new EntityTextures();
    }
}

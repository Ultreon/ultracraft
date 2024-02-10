package com.ultreon.craft.client.model.entity.renderer;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.init.Shaders;
import com.ultreon.craft.client.model.EntityModelInstance;
import com.ultreon.craft.client.model.WorldRenderContext;
import com.ultreon.craft.client.model.entity.EntityModel;
import com.ultreon.craft.client.render.EntityTextures;
import com.ultreon.craft.entity.DroppedItem;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DroppedItemRenderer extends EntityRenderer<@NotNull DroppedItem> {
    public DroppedItemRenderer(EntityModel<@NotNull DroppedItem> droppedItemModel, Model model) {
        super();
    }

    @Override
    public void animate(EntityModelInstance<@NotNull DroppedItem> instance, WorldRenderContext<@NotNull DroppedItem> context) {
        DroppedItem entity = instance.getEntity();
        int age = entity.getAge();
        float rotation = age * 5f % 360;
        float translation = MathUtils.sinDeg(age % 180 * 2) / 8f;

        instance.rotateY(rotation);
        instance.translate(0, translation, 0);
        instance.scale(-0.15f, -0.15f, -0.15f);
        instance.translate(0.5, 0, -0.5);
    }

    @Override
    public ModelInstance createModel(@NotNull DroppedItem entity) {
        if (entity.getStack().isEmpty()) {
            UltracraftClient.LOGGER.warn("Tried to render empty item stack");
            return null;
        }

        ModelInstance modelInstance = Objects.requireNonNull(client.itemRenderer.createModelInstance(entity.getStack()));
        modelInstance.userData = Shaders.MODEL_VIEW;
        return modelInstance;
    }

    @Override
    public EntityTextures getTextures() {
        return new EntityTextures();
    }
}

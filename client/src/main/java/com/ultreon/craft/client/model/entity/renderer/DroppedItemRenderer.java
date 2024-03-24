package com.ultreon.craft.client.model.entity.renderer;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.model.entity.EntityModel;
import com.ultreon.craft.client.model.entity.PlayerModel;
import com.ultreon.craft.client.render.EntityTextures;
import com.ultreon.craft.entity.DroppedItem;
import com.ultreon.craft.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DroppedItemRenderer extends EntityRenderer<@NotNull DroppedItem> {
    public DroppedItemRenderer(EntityModel<@NotNull DroppedItem> droppedItemModel, Model model) {
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

        UltracraftClient.LOGGER.debug("Age: " + age + ", rotation: " + rotation + ", translation: " + translation);

//        instance.transform.translate(0, translation, 0).scale(16, 16, 16).rotate(Vector3.Y, rotation);
        instance.transform.setToTranslationAndScaling(10, 10, 10, 64, 64, 64);
    }

    @Override
    public void render(ModelInstance instance, Array<Renderable> output, Pool<Renderable> renderablePool) {
        super.render(instance, output, renderablePool);
    }

    @Override
    public ModelInstance createInstance(@NotNull DroppedItem entity) {
        if (entity.getStack().isEmpty()) {
            UltracraftClient.LOGGER.warn("Tried to render empty item stack");
            return null;
        }

        return Objects.requireNonNull(client.itemRenderer.createModelInstance(entity.getStack()));
    }

    @Override
    public EntityTextures getTextures() {
        return new EntityTextures();
    }
}

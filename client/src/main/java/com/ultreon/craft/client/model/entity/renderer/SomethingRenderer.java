package com.ultreon.craft.client.model.entity.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.init.Shaders;
import com.ultreon.craft.client.model.EntityModelInstance;
import com.ultreon.craft.client.model.WorldRenderContext;
import com.ultreon.craft.client.model.entity.EntityModel;
import com.ultreon.craft.client.render.EntityTextures;
import com.ultreon.craft.entity.Something;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SomethingRenderer extends LivingEntityRenderer<@NotNull Something> {
    public SomethingRenderer(EntityModel<@NotNull Something> droppedItemModel, Model model) {
        super(droppedItemModel, model);
    }

    @Override
    public void animate(EntityModelInstance<@NotNull Something> instance, WorldRenderContext<@NotNull Something> context) {
        Node bone = instance.getNode("bone");
        if (bone != null) {
//            bone.rotation.add(new Quaternion().setFromAxis(Vector3.Y, Gdx.graphics.getDeltaTime()));
        } else {
            UltracraftClient.LOGGER.warn("Bone not found in model for entity {}", instance.getEntity().getId());
        }
    }

    @Override
    public EntityTextures getTextures() {
        return new EntityTextures();
    }
}
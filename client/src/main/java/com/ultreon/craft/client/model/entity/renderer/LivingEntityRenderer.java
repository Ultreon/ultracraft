package com.ultreon.craft.client.model.entity.renderer;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.ultreon.craft.client.model.entity.EntityModel;
import com.ultreon.craft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public abstract class LivingEntityRenderer<E extends LivingEntity> extends EntityRenderer<E> {
    private final EntityModel<E> entityModel;
    private final Model model;

    protected LivingEntityRenderer(EntityModel<E> entityModel, Model model) {
        super();

        this.entityModel = entityModel;
        this.model = model;
    }

    @Override
    public @Nullable ModelInstance createInstance(E entity) {
        return new ModelInstance(model);
    }

    public EntityModel<E> getEntityModel() {
        return entityModel;
    }
}

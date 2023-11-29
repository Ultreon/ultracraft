package com.ultreon.craft.client.model.entity.renderer;

import com.ultreon.craft.client.model.entity.LivingEntityModel;
import com.ultreon.craft.entity.LivingEntity;

public abstract class LivingEntityRenderer<M extends LivingEntityModel<?>, E extends LivingEntity> extends EntityRenderer<M, E> {
    protected LivingEntityRenderer(M model) {
        super(model);
    }
}

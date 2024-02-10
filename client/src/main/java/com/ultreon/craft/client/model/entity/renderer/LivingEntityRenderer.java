package com.ultreon.craft.client.model.entity.renderer;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.google.common.base.Preconditions;
import com.ultreon.craft.client.model.entity.EntityModel;
import com.ultreon.craft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public abstract class LivingEntityRenderer<E extends LivingEntity> extends EntityRenderer<E> {
    private final EntityModel<E> entityModel;
    private final Model model;

    protected LivingEntityRenderer(EntityModel<E> entityModel, Model model) {
        super();

        Preconditions.checkNotNull(model, "Model cannot be null");

        this.entityModel = entityModel;
        this.model = model;
    }

    public static void updateWalkAnim(LivingEntity player, float walkAnim, float delta, float duration) {
        player.walking = true;
        float old = walkAnim;
        walkAnim -= player.inverseAnim ? delta : -delta;

        if (walkAnim > duration) {
            float overflow = duration - walkAnim;
            walkAnim = duration - overflow;

            player.inverseAnim = true;
        } else if (walkAnim < -duration) {
            float overflow = duration + walkAnim;
            walkAnim = -duration - overflow;
            player.inverseAnim = false;
        }

        if (!player.isWalking() && (old >= 0 && walkAnim < 0 || old <= 0 && walkAnim > 0)) {
            player.walking = false;
        }

        player.walkAnim = walkAnim;
    }

    @Override
    public @Nullable ModelInstance createModel(E entity) {
        return new ModelInstance(model);
    }

    public EntityModel<E> getEntityModel() {
        return entityModel;
    }
}

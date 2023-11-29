package com.ultreon.craft.client.registry;

import com.ultreon.craft.client.model.entity.EntityModel;
import com.ultreon.craft.client.model.entity.renderer.EntityRenderer;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RendererRegistry {
    private static final Map<EntityType<?>, EntityRenderer<?, ?>> registry = new HashMap<>();

    public static <T extends Entity> void register(EntityType<@NotNull T> entityType, EntityRenderer<? extends EntityModel<? extends T>, T> model) {
        RendererRegistry.registry.put(entityType, model);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> EntityRenderer<EntityModel<? extends T>, T> get(EntityType<@NotNull T> entityType) {
        return (EntityRenderer<EntityModel<? extends T>, T>) RendererRegistry.registry.get(entityType);
    }
}

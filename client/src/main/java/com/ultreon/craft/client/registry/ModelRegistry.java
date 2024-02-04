package com.ultreon.craft.client.registry;

import com.badlogic.gdx.graphics.g3d.Model;
import com.ultreon.craft.client.model.entity.EntityModel;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ModelRegistry {
    private static final Map<EntityType<?>, EntityModel<?>> registry = new HashMap<>();
    private static final Map<EntityType<?>, Model> finishedRegistry = new HashMap<>();

    public static <T extends Entity> void register(EntityType<@NotNull T> entityType, EntityModel<T> model) {
        ModelRegistry.registry.put(entityType, model);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends Entity> EntityModel<T> get(EntityType<@NotNull T> entityType) {
        return (EntityModel<T>) ModelRegistry.registry.get(entityType);
    }

    public static void registerFinished(EntityType<?> value, Model finished) {
        ModelRegistry.finishedRegistry.put(value, finished);
    }

    public static Model getFinished(EntityType<?> value) {
        return ModelRegistry.finishedRegistry.get(value);
    }
}

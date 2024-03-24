package com.ultreon.craft.client.registry;

import com.badlogic.gdx.graphics.g3d.Model;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.model.entity.EntityModel;
import com.ultreon.craft.client.model.entity.renderer.EntityRenderer;
import com.ultreon.craft.collection.OrderedMap;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class RendererRegistry {
    private static final Map<EntityType<?>, BiFunction<EntityModel<?>, Model, EntityRenderer<?>>> REGISTRY = new HashMap<>();
    private static final Map<EntityType<?>, EntityRenderer<?>> FINISHED_REGISTRY = new OrderedMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Entity, M extends EntityModel<T>> void register(EntityType<@NotNull T> entityType, BiFunction<M, Model, EntityRenderer<T>> factory) {
        RendererRegistry.REGISTRY.put(entityType, (entityModel, model) -> factory.apply((M) entityModel, model));
    }

    public static void load() {
        for (var e : RendererRegistry.REGISTRY.entrySet()) {
            UltracraftClient.LOGGER.debug("Registering renderer for entity {}", e.getKey().getId());
            RendererRegistry.FINISHED_REGISTRY.put(e.getKey(), e.getValue().apply(ModelRegistry.get(e.getKey()), ModelRegistry.getFinished(e.getKey())));
        }
    }

    public static <T extends Entity> EntityRenderer<?> get(EntityType<@NotNull T> entityType) {
        return RendererRegistry.FINISHED_REGISTRY.get(entityType);
    }
}

package com.ultreon.craft.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.input.GameCamera;
import com.ultreon.craft.render.entity.EntityModelContext;
import com.ultreon.craft.render.entity.EntityRenderer;
import com.ultreon.libs.commons.v0.size.IntSize;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EntityRenderDispatcher {
    private static final Map<EntityType<?>, EntityRenderer<?>> ENTITIY_RENDERERS = new HashMap<>();
    private final GameCamera camera;

    public EntityRenderDispatcher(GameCamera camera) {
        this.camera = camera;
    }

    public static <T extends Entity> void register(EntityType<@NotNull T> entityType, IntSize texSize, Function<EntityModelContext, EntityRenderer<T>> factory) {
        ENTITIY_RENDERERS.put(entityType, factory.apply(new EntityModelContext(texSize)));
    }

    public void setupRenderers() {
        for (EntityRenderer<?> renderer : ENTITIY_RENDERERS.values()) {
            renderer.build();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> void render(Array<Renderable> renderables, Pool<Renderable> pool, T entity) {
        EntityRenderer<T> entityRenderer = (EntityRenderer<T>) ENTITIY_RENDERERS.get(entity.getType());
        Player player = UltreonCraft.get().player;
        if (entityRenderer == null || player == null) return;

        entityRenderer.getRenderables(renderables, entity, pool, camera.getOffsetPos(entity.getPosition(), player), 16);
    }
}

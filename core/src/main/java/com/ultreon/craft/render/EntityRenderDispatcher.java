package com.ultreon.craft.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.input.GameCamera;
import com.ultreon.craft.render.entity.EntityRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class EntityRenderDispatcher {
    private static final Map<EntityType<?>, EntityRenderer<?>> ENTITIY_RENDERERS = new HashMap<>();
    private final GameCamera camera;

    public EntityRenderDispatcher(GameCamera camera) {
        this.camera = camera;
    }

    public static <T extends Entity> void register(EntityType<@NotNull T> entityType, EntityRenderer<T> renderer) {
        ENTITIY_RENDERERS.put(entityType, renderer);
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> void render(Array<Renderable> renderables, Pool<Renderable> pool, T entity) {
        EntityRenderer<T> entityRenderer = (EntityRenderer<T>) ENTITIY_RENDERERS.get(entity.getType());
        Player player = UltreonCraft.get().player;
        if (entityRenderer == null || player == null) return;

        entityRenderer.getRenderables(renderables, entity, pool, camera.getOffsetPos(entity.getPosition(), player), 16);
    }
}

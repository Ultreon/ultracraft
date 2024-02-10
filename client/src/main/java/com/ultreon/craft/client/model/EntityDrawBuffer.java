package com.ultreon.craft.client.model;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.ultreon.craft.client.render.RenderContext;
import com.ultreon.craft.entity.Entity;

public class EntityDrawBuffer implements RenderContext<Entity> {
    private Entity entity;

    public EntityDrawBuffer(Entity entity) {
        this.entity = entity;
    }

    @Override
    public Entity getHolder() {
        return entity;
    }

    @Override
    public void render(RenderableProvider renderableProvider) {

    }
}

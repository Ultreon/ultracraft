package com.ultreon.craft.client.model;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.ultreon.craft.client.render.RenderContext;
import com.ultreon.craft.world.World;

public interface WorldRenderContext<T> extends RenderContext<T> {
    World getWorld();
}

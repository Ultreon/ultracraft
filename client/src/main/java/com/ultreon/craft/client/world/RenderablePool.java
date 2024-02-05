package com.ultreon.craft.client.world;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Pool;

class RenderablePool extends Pool<Renderable> {
    @Override
    protected Renderable newObject() {
        return new Renderable();
    }
}

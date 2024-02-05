package com.ultreon.craft.client.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.ultreon.craft.client.util.RenderableArray;

public record ModelObject(ShaderProvider modelView, RenderableArray renderables) {

    public void dispose() {
        for (Renderable renderable : renderables) {
            renderable.meshPart.mesh = null;
            renderable.userData = null;
        }
        renderables.clear();
    }
}

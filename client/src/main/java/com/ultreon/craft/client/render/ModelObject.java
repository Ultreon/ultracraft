package com.ultreon.craft.client.render;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.ultreon.craft.client.render.shader.OpenShaderProvider;
import com.ultreon.craft.client.util.RenderableArray;

public record ModelObject(OpenShaderProvider shaderProvider, ModelInstance model, RenderableArray renderables) {

    public void dispose() {
        for (Renderable renderable : renderables) {
            renderable.meshPart.mesh = null;
            renderable.userData = null;
        }
        renderables.clear();
    }

    @Override
    public String toString() {
        return "ModelObject[" +
                "shaderProvider=" + shaderProvider + ", " +
                "renderables=" + renderables + ']';
    }

}

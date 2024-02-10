package com.ultreon.craft.client.render;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.ultreon.craft.client.render.shader.OpenShaderProvider;

public record ModelObject(OpenShaderProvider shaderProvider, ModelInstance model) {
    @Override
    public String toString() {
        return "ModelObject[" + "shaderProvider=" + shaderProvider + ']';
    }

}

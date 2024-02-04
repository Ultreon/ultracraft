package com.ultreon.craft.client.render.shader;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;

public interface OpenShaderProvider {
    Shader createShader(Renderable renderable);
}

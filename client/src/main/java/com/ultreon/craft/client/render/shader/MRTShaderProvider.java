package com.ultreon.craft.client.render.shader;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;

public class MRTShaderProvider extends BaseShaderProvider implements OpenShaderProvider {
    @Override
    public Shader createShader(Renderable renderable) {
        return new MRTShader(renderable);
    }
}

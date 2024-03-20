package com.ultreon.craft.client.render.shader;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import com.ultreon.craft.client.render.ShaderContext;

public class GameShaderProvider extends BaseShaderProvider {
    private DepthShader.Config config;

    public GameShaderProvider(DepthShader.Config config) {
        this.config = config;
    }

    @Override
    protected Shader createShader(Renderable renderable) {
        OpenShaderProvider openShaderProvider = ShaderContext.get();
        Shader shader = openShaderProvider.createShader(renderable);
        if (shader == null) throw new IllegalStateException("Shader not found");
        if (!shader.canRender(renderable)) throw new IllegalStateException("Shader cannot render");
        return shader;

//        ShaderMode mode = ShaderContext.get();
//        if (mode == ShaderMode.DEPTH) {
//            return new DepthShader(renderable, this.config);
//        }
//
//        return new DefaultShader(renderable, this.config, DefaultShader.createPrefix(renderable, config), ShaderPrograms.DEFAULT.getVertexShaderSource(), ShaderPrograms.DEFAULT.getFragmentShaderSource());
    }
}

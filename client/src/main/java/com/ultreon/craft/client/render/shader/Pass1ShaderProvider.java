package com.ultreon.craft.client.render.shader;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.world.ChunkMesh;

import static com.badlogic.gdx.graphics.GL20.GL_BACK;

public class Pass1ShaderProvider extends DepthShaderProvider implements OpenShaderProvider {
    public Pass1ShaderProvider() {
        super(Pass1ShaderProvider.configure());
    }

    private static Pass1Shader.Config configure() {
        var config = new Pass1Shader.Config();
        config.defaultCullFace = GL_BACK;
        return config;
    }

    @Override
    public Shader createShader(Renderable renderable) {
//        if (renderable.userData instanceof ChunkMesh) {
        Pass1Shader mainShader = new Pass1Shader(renderable, (Pass1Shader.Config) this.config);
        this.checkShaderCompilation(mainShader.program);
        return mainShader;
//        }

//        DefaultShader shader = new DefaultShader(renderable, this.config);
//        this.checkShaderCompilation(shader.program);
//        return shader;
    }


    protected void checkShaderCompilation(ShaderProgram program) {
        String shaderLog = program.getLog();
        if (program.isCompiled()) {
            if (shaderLog.isEmpty()) UltracraftClient.LOGGER.debug("Shader compilation success");
            else UltracraftClient.LOGGER.warn("Shader compilation warnings:\n{}", shaderLog);
        } else throw new GdxRuntimeException("Shader compilation failed:\n" + shaderLog);
    }
}

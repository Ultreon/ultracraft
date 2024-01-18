package com.ultreon.craft.client.shaders.provider;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.init.Shaders;
import com.ultreon.craft.client.render.shader.OpenShaderProvider;
import com.ultreon.craft.client.shaders.ModelViewShader;
import com.ultreon.craft.client.shaders.WorldShader;
import com.ultreon.craft.client.world.ClientChunk;

public class ModelViewShaderProvider extends DefaultShaderProvider implements OpenShaderProvider {
    public ModelViewShaderProvider(final DefaultShader.Config config) {
        super(config);
    }

    public ModelViewShaderProvider(final String vertexShader, final String fragmentShader) {
        this(new DefaultShader.Config(vertexShader, fragmentShader));
    }

    public ModelViewShaderProvider(final FileHandle vertexShader, final FileHandle fragmentShader) {
        this(vertexShader.readString(), fragmentShader.readString());
    }

    public ModelViewShaderProvider() {
        this(null);
    }

    @Override
    public Shader createShader(Renderable renderable) {
        if (renderable != null) {
            ModelViewShader worldShader = new ModelViewShader(renderable, this.config);
            Shaders.checkShaderCompilation(worldShader.program);
            return worldShader;
        }

        assert renderable != null;
        return new DefaultShader(renderable, new DefaultShader.Config());
    }
}

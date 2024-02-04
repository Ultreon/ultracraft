package com.ultreon.craft.client.shaders.provider;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.ultreon.craft.client.init.Shaders;
import com.ultreon.craft.client.render.shader.OpenShaderProvider;
import com.ultreon.craft.client.shaders.WorldShader;
import com.ultreon.craft.client.world.ClientChunk;

public class WorldShaderProvider extends DefaultShaderProvider implements OpenShaderProvider {
    public WorldShaderProvider(final DefaultShader.Config config) {
        super(config);
    }

    public WorldShaderProvider(final String vertexShader, final String fragmentShader) {
        this(new DefaultShader.Config(vertexShader, fragmentShader));
    }

    public WorldShaderProvider(final FileHandle vertexShader, final FileHandle fragmentShader) {
        this(vertexShader.readString(), fragmentShader.readString());
    }

    public WorldShaderProvider() {
        this(null);
    }

    @Override
    public Shader createShader(Renderable renderable) {
        if (renderable != null && renderable.userData instanceof ClientChunk) {
            WorldShader worldShader = new WorldShader(renderable, this.config);
            Shaders.checkShaderCompilation(worldShader.program, "WorldShader");
            return worldShader;
        }

        if (renderable != null && renderable.userData instanceof OpenShaderProvider provider) {
            return provider.createShader(renderable);
        }

        assert renderable != null;
        return new DefaultShader(renderable, new DefaultShader.Config());
    }

}

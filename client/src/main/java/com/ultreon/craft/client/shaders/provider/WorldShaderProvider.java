package com.ultreon.craft.client.shaders.provider;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.ultreon.craft.client.events.ClientChunkEvents;
import com.ultreon.craft.client.init.Shaders;
import com.ultreon.craft.client.render.ModelObject;
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

        if (renderable != null) {
            return getShaderFromUserData(renderable, renderable.userData);
        }

        throw new NullPointerException("Renderable cannot be null");
    }

    private static Shader getShaderFromUserData(Renderable renderable, Object userData) {
        return switch (userData) {
            case OpenShaderProvider provider -> provider.createShader(renderable);
            case Shader shader -> shader;
            case ModelObject modelObject -> modelObject.shaderProvider().createShader(renderable);
            case null, default -> new DefaultShader(renderable, new DefaultShader.Config());
        };
    }

}

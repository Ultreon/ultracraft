package com.ultreon.craft.client.render.pipeline;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.google.common.base.Supplier;
import com.ultreon.craft.client.init.Shaders;
import com.ultreon.craft.client.input.GameCamera;
import com.ultreon.craft.client.shaders.provider.WorldShaderProvider;
import org.checkerframework.common.reflection.qual.NewInstance;

public class WorldDiffuseNode extends WorldRenderNode {
    private final Supplier<WorldShaderProvider> shaderProvider = Shaders.WORLD;

    @NewInstance
    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input) {
        this.render(modelBatch, this.shaderProvider.get(), input);
        textures.put("diffuse", this.getFrameBuffer().getColorBufferTexture());
        return input;
    }
}

package com.ultreon.craft.client.render.pipeline;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.ultreon.craft.client.input.GameCamera;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.client.world.WorldRenderer;
import org.checkerframework.common.reflection.qual.NewInstance;

import static com.ultreon.craft.client.UltracraftClient.LOGGER;

public class CollectNode extends WorldRenderNode {
    @NewInstance
    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input) {
        LocalPlayer localPlayer = this.client.player;
        WorldRenderer worldRenderer = this.client.worldRenderer;
        if (localPlayer == null || worldRenderer == null) {
            LOGGER.warn("worldRenderer or localPlayer is null");
            return input;
        }
        worldRenderer.collectEntity(localPlayer, input, this.pool());
        worldRenderer.collect(input, this.pool());
        return input;
    }

    @Override
    public void flush() {
    }
}

package com.ultreon.craft.client.render.pipeline;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.input.GameCamera;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.client.world.WorldRenderer;
import com.ultreon.craft.debug.ValueTracker;
import com.ultreon.craft.entity.Entity;
import org.checkerframework.common.reflection.qual.NewInstance;

import java.util.ArrayList;
import java.util.List;

import static com.ultreon.craft.client.UltracraftClient.LOGGER;

public class CollectNode extends RenderPipeline.RenderNode {
    @NewInstance
    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input) {
        var localPlayer = this.client.player;
        var worldRenderer = this.client.worldRenderer;
        var world = this.client.world;
        if (localPlayer == null || worldRenderer == null || world == null) {
            LOGGER.warn("worldRenderer or localPlayer is null");
            return input;
        }
        var position = localPlayer.getPosition();
        List<Entity> toSort = new ArrayList<>(world.getAllEntities());
        worldRenderer.collect(input, this.pool());
        toSort.sort((e1, e2) -> {
            var d1 = e1.getPosition().dst(position);
            var d2 = e2.getPosition().dst(position);
            return Double.compare(d1, d2);
        });
        for (Entity entity : toSort) {
            worldRenderer.collectEntity(entity, input, this.pool());
        }

        ValueTracker.setObtainedRenderables(this.pool().getObtained());
        return input;
    }
}

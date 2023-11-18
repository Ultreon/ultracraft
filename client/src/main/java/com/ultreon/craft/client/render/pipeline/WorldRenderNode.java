package com.ultreon.craft.client.render.pipeline;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.ultreon.craft.client.ShaderContext;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.client.render.shader.OpenShaderProvider;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.client.world.WorldRenderer;
import com.ultreon.libs.commons.v0.vector.Vec3d;

import java.io.PrintStream;

import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_NONE;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE0;

public abstract class WorldRenderNode extends RenderPipeline.RenderNode {
    private Shader shader;

    public void setTexture(Texture texture) {
        if (texture == null) {
            gl.glActiveTexture(GL_NONE);
        }
        texture.bind(GL_TEXTURE0);
    }

    protected void render(ModelBatch modelBatch, ShaderProvider shaderProvider, Array<Renderable> input) {
        for (Renderable renderable : input) {
            if (!(shaderProvider instanceof OpenShaderProvider openShaderProvider))
                throw new IllegalStateException("Shader provider is not open");
            ShaderContext.set(openShaderProvider);
            renderable.shader = null;
            this.shader = shaderProvider.getShader(renderable);
            if (this.shader == null) throw new IllegalStateException("Shader not found");
            renderable.shader = this.shader;
            modelBatch.render(renderable);
        }
    }

    public void renderWorld(ModelBatch batch) {
        ClientWorld world = this.client.world;
        WorldRenderer worldRenderer = this.client.worldRenderer;
        LocalPlayer localPlayer = this.client.player;

        if (world != null && worldRenderer != null && this.client.renderWorld && localPlayer != null) {
            this.renderWorldOnce(worldRenderer, world, localPlayer.getPosition(), batch);
        }
    }

    @Override
    public void dumpInfo(PrintStream stream) {
        super.dumpInfo(stream);
        Shader shader = this.shader;
        if (shader != null) {
            stream.println("Shader Hash Code: " + shader.hashCode());
            stream.println("Shader Classname: " + shader.getClass().getName());
            stream.println("Shader Superclass Classname: " + shader.getClass().getSuperclass().getName());
            stream.println("Shader String: " + shader.toString());
        }
    }

    private void renderWorldOnce(WorldRenderer worldRenderer, ClientWorld world, Vec3d position, ModelBatch batch) {
        worldRenderer.renderEntities();

        world.getAllEntities().sorted((e1, e2) -> {
            var d1 = e1.getPosition().dst(position);
            var d2 = e2.getPosition().dst(position);
            return Double.compare(d1, d2);
        }).forEachOrdered(entity -> batch.render((output, pool) -> worldRenderer.collectEntity(entity, output, pool)));

        batch.render(worldRenderer::collect, worldRenderer.getEnvironment());

        UltracraftClient.PROFILER.section("(Local Player)", () -> {
            LocalPlayer localPlayer = this.client.player;
            if (localPlayer == null || !this.client.isInThirdPerson()) return;

            batch.render((output, pool) -> worldRenderer.collectEntity(localPlayer, output, pool));
        });
    }
}

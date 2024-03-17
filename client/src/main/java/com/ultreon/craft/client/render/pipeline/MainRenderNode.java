package com.ultreon.craft.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.ultreon.craft.client.config.Config;
import com.ultreon.craft.client.imgui.ImGuiOverlay;
import com.ultreon.craft.client.init.ShaderPrograms;
import com.ultreon.craft.client.input.GameCamera;
import com.ultreon.craft.client.render.pipeline.RenderPipeline.RenderNode;
import com.ultreon.libs.commons.v0.Mth;
import org.checkerframework.common.reflection.qual.NewInstance;

import java.io.PrintStream;

import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE0;

public class MainRenderNode extends RenderNode {
    private Mesh quad = this.createFullScreenQuad();
    private final ShaderProgram program = ShaderPrograms.MODEL;
    private float blurScale = 0f;

    @NewInstance
    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input) {
        Texture depthMap = textures.get("depth");
        Texture skyboxTexture = textures.get("skybox");
        Texture diffuseTexture = textures.get("diffuse");

        diffuseTexture.bind(0);

        modelBatch.end();
        this.client.renderer.begin();

        var blurScale = this.blurScale;
        blurScale += client.screen != null ? Gdx.graphics.getDeltaTime() * 3f : -Gdx.graphics.getDeltaTime() * 3f;

        blurScale = Mth.clamp(blurScale, 0f, 1f);
        this.blurScale = blurScale;

        if (blurScale > 0f) {
            this.client.renderer.blurred(blurScale, Config.blurRadius * blurScale, true, 1, () -> this.drawDiffuse(diffuseTexture));
        } else {
            this.drawDiffuse(diffuseTexture);
        }
        this.client.renderer.end();
        this.client.spriteBatch.setShader(null);

        gl.glActiveTexture(GL_TEXTURE0);

        if (ImGuiOverlay.SHOW_RENDER_PIPELINE.get()) {
            this.client.renderer.begin();
            this.client.spriteBatch.draw(depthMap, (float) (3 * Gdx.graphics.getWidth()) / 4, 0, (float) Gdx.graphics.getWidth() / 4, (float) Gdx.graphics.getHeight() / 4);
            this.client.spriteBatch.flush();
            this.client.spriteBatch.draw(diffuseTexture, (float) (2 * Gdx.graphics.getWidth()) / 4, 0, (float) Gdx.graphics.getWidth() / 4, (float) Gdx.graphics.getHeight() / 4);
            this.client.spriteBatch.flush();
            this.client.spriteBatch.draw(skyboxTexture, (float) Gdx.graphics.getWidth() / 4, 0, (float) Gdx.graphics.getWidth() / 4, (float) Gdx.graphics.getHeight() / 4);
            this.client.renderer.end();
        }

        return input;
    }

    private void drawDiffuse(Texture diffuseTexture) {
        this.client.spriteBatch.setShader(this.program);
        this.client.spriteBatch.draw(diffuseTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        this.quad.dispose();
        this.quad = this.createFullScreenQuad();
    }

    @Override
    public void dumpInfo(PrintStream stream) {
        super.dumpInfo(stream);
        stream.println("Shader Handle: " + this.program.getHandle());
    }

    public Mesh createFullScreenQuad() {
        float[] vertices = new float[20];
        int i = 0;

        vertices[i++] = -1;
        vertices[i++] = -1;
        vertices[i++] = 0;
        vertices[i++] = 0f;
        vertices[i++] = 0f;

        vertices[i++] = 1f;
        vertices[i++] = -1;
        vertices[i++] = 0;
        vertices[i++] = 1f;
        vertices[i++] = 0f;

        vertices[i++] = 1f;
        vertices[i++] = 1f;
        vertices[i++] = 0;
        vertices[i++] = 1f;
        vertices[i++] = 1f;

        vertices[i++] = -1;
        vertices[i++] = 1f;
        vertices[i++] = 0;
        vertices[i++] = 0f;
        vertices[i] = 1f;

        Mesh mesh = new Mesh(true, 4, 0,
                VertexAttribute.Position(),
                VertexAttribute.TexCoords(0)
        );

        mesh.setVertices(vertices);
        return mesh;
    }
}

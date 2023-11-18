package com.ultreon.craft.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.ultreon.craft.client.init.ShaderPrograms;
import com.ultreon.craft.client.input.GameCamera;
import com.ultreon.craft.client.render.pipeline.RenderPipeline.RenderNode;
import org.checkerframework.common.reflection.qual.NewInstance;

import java.io.PrintStream;

import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.GL20.GL_TEXTURE0;

public class MainRenderNode extends RenderNode {
    private Mesh quad = this.createFullScreenQuad();
    private final ShaderProgram program = ShaderPrograms.MODEL;

    @NewInstance
    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input) {
        Texture depthMap = textures.get("depth");
        Texture diffuseTexture = textures.get("diffuse");
        Texture ssaoTexture = textures.get("ssao");

        diffuseTexture.bind(0);
        ssaoTexture.bind(1);

        modelBatch.end();
        this.client.spriteBatch.begin();
        this.client.spriteBatch.setShader(this.program);
        this.program.setUniformi("u_ssaoMap", 1);
        this.program.setUniformf("u_resolution", new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        this.client.spriteBatch.draw(diffuseTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.client.spriteBatch.end();
        this.client.spriteBatch.setShader(null);

        textures.clear();

        gl.glActiveTexture(GL_TEXTURE0);

        this.client.spriteBatch.begin();
        this.client.spriteBatch.draw(depthMap, (float) (3 * Gdx.graphics.getWidth()) / 4, 0, (float) Gdx.graphics.getWidth() / 4, (float) Gdx.graphics.getHeight() / 4);
        this.client.spriteBatch.flush();
        this.client.spriteBatch.draw(ssaoTexture, (float) (Gdx.graphics.getWidth()) / 4, 0, (float) Gdx.graphics.getWidth() / 4, (float) Gdx.graphics.getHeight() / 4);
        this.client.spriteBatch.flush();
        this.client.spriteBatch.draw(ssaoTexture, 0, 0, (float) Gdx.graphics.getWidth() / 4, (float) Gdx.graphics.getHeight() / 4);
        this.client.spriteBatch.flush();
        this.client.spriteBatch.draw(diffuseTexture, (float) (2 * Gdx.graphics.getWidth()) / 4, 0, (float) Gdx.graphics.getWidth() / 4, (float) Gdx.graphics.getHeight() / 4);
        this.client.spriteBatch.end();

        return input;
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

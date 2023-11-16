package com.ultreon.craft.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.ultreon.craft.client.init.ShaderPrograms;
import com.ultreon.craft.client.input.GameCamera;
import org.checkerframework.common.reflection.qual.NewInstance;

import java.io.PrintStream;

import static com.badlogic.gdx.graphics.GL20.*;

public class MainRenderNode extends RenderPipeline.RenderNode {
    private Mesh quad = this.createFullScreenQuad();
    private final ShaderProgram program = ShaderPrograms.MODEL;

    @NewInstance
    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input) {
        Texture depthMap = textures.get("depth");
        Texture diffuseTexture = textures.get("diffuse");
        Texture ssaoTexture = textures.get("ssao");

        this.client.spriteBatch.begin();
        this.client.spriteBatch.draw(depthMap, (float) (3 * Gdx.graphics.getWidth()) / 4, 0, (float) Gdx.graphics.getWidth() / 4, (float) Gdx.graphics.getHeight() / 4);
        this.client.spriteBatch.flush();
        this.client.spriteBatch.draw(diffuseTexture, (float) (2 * Gdx.graphics.getWidth()) / 4, 0, (float) Gdx.graphics.getWidth() / 4, (float) Gdx.graphics.getHeight() / 4);
        this.client.spriteBatch.flush();
        this.client.spriteBatch.draw(ssaoTexture, (float) (Gdx.graphics.getWidth()) / 4, 0, (float) Gdx.graphics.getWidth() / 4, (float) Gdx.graphics.getHeight() / 4);
        this.client.spriteBatch.end();

        diffuseTexture.bind(0);
        depthMap.bind(1);

        this.program.setUniformi("u_diffuseTexture", this.textureBinder.bind(diffuseTexture));
        this.program.setUniformi("u_depthTexture", this.textureBinder.bind(depthMap));
        this.program.setUniformMatrix("u_inverseProjectionMatrix", this.client.camera.invProjectionView);
        this.program.setUniformMatrix("u_projTrans", this.client.camera.projection);
        this.program.setUniformMatrix("u_viewTrans", this.client.camera.view);
        this.program.setUniformMatrix("u_worldTrans", this.IDENTITY_MATRIX);
        this.program.setUniformf("u_resolution", new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        this.quad.render(this.program, GL_TRIANGLE_FAN);
        textures.clear();

        Gdx.gl.glActiveTexture(GL_TEXTURE0);

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
                new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
        );

        mesh.setVertices(vertices);
        return mesh;
    }
}

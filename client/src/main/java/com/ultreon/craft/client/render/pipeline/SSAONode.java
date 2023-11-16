package com.ultreon.craft.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
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

public class SSAONode extends RenderNode {
    private Mesh quad = this.createFullScreenQuad();
    private final ShaderProgram program = ShaderPrograms.SSAO;

    @NewInstance
    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input) {
        Texture texture = textures.get("depth");
        texture.bind(0);

        this.program.setUniformi("u_depthMap", this.textureBinder.bind(texture));
        this.program.setUniformMatrix("u_inverseProjectionMatrix", this.client.camera.invProjectionView);
        this.program.setUniformMatrix("u_viewTrans", this.client.camera.view);
        this.program.setUniformMatrix("u_projTrans", this.client.camera.projection);
        this.program.setUniformMatrix("u_projViewTrans", this.client.camera.combined);
        this.program.setUniformMatrix("u_worldTrans", RenderNode.IDENTITY_MATRIX);
        this.program.setUniformf("u_resolution", new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        this.quad.render(this.program, GL20.GL_TRIANGLE_FAN);

        textures.put("ssao", this.getFrameBuffer().getColorBufferTexture());
        return input;
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
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
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
        );

        mesh.setVertices(vertices);
        return mesh;
    }
}

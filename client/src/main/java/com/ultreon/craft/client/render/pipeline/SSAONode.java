package com.ultreon.craft.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.ultreon.craft.client.imgui.ImGuiOverlay;
import com.ultreon.craft.client.init.ShaderPrograms;
import com.ultreon.craft.client.input.GameCamera;
import org.checkerframework.common.reflection.qual.NewInstance;

import java.io.PrintStream;

public class SSAONode extends WorldRenderNode {
    private Mesh quad = this.createFullScreenQuad();
    private final ShaderProgram program = ShaderPrograms.SSAO;

    @NewInstance
    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input) {
//        this.render(modelBatch, this.shaderProvider, input);
//        textures.put("ssao", this.getFrameBuffer().getColorBufferTexture());
//        return input;
        Texture texture = textures.get("depth");
        texture.bind(0);

        this.client.spriteBatch.begin();
        this.client.spriteBatch.setShader(this.program);
        this.program.setUniformf("u_resolution", new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        this.program.setUniformf("u_radius", ImGuiOverlay.U_RADIUS.get());
        this.program.setUniformf("u_intensity", ImGuiOverlay.U_INTENSITY.get());
        this.program.setUniformf("u_cap", ImGuiOverlay.U_CAP.get());
        this.program.setUniformf("u_multiplier", ImGuiOverlay.U_MULTIPLIER.get());
        this.program.setUniformf("u_depth_tolerance", ImGuiOverlay.U_DEPTH_TOLERANCE.get());
        this.client.spriteBatch.draw(texture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.client.spriteBatch.end();
        this.client.spriteBatch.setShader(null);

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
        stream.println("Shader HashCode: " + this.program.hashCode());
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

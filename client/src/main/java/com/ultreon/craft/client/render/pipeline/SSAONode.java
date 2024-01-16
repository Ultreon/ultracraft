package com.ultreon.craft.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.ultreon.craft.client.init.ShaderPrograms;
import com.ultreon.craft.client.input.GameCamera;
import org.checkerframework.common.reflection.qual.NewInstance;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.Random;

public class SSAONode extends RenderPipeline.RenderNode {
    private static final Texture RAND_TEX = new Texture("noise.png");
    private Mesh quad = this.createFullScreenQuad();
    private final ShaderProgram program = ShaderPrograms.WORLD;
    private final Random rand = new Random();
    private static final Texture defTex0 = new Texture(1, 1, Pixmap.Format.RGB888); // Default texture
    private static final Texture defTex1 = new Texture(1, 1, Pixmap.Format.RGB888); // Default texture
    private static final Texture defTex2 = new Texture(1, 1, Pixmap.Format.RGB888); // Default texture
    private static final Texture defTex3 = new Texture(1, 1, Pixmap.Format.RGB888); // Default texture
    private Texture tex0 = SSAONode.defTex0;
    private Texture tex1 = SSAONode.defTex1;
    private Texture tex2 = SSAONode.defTex2;
    private Texture tex3 = SSAONode.defTex3;

    @NewInstance
    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input) {
//        this.tex0 = textures.get("depth");
//        this.tex0.bind(0);
//
//        this.tex1 = SSAONode.RAND_TEX;
//        this.tex1.bind(1);
//
//        this.tex2 = SSAONode.RAND_TEX;
//        this.tex2.bind(2);
//
//        this.tex3 = SSAONode.RAND_TEX;
//        this.tex3.bind(3);
//
//        this.client.spriteBatch.begin();
//        this.client.spriteBatch.setShader(this.program);
//
//        this.setUniforms();
//
//        this.client.spriteBatch.draw(SSAONode.defTex0, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        this.client.spriteBatch.end();
//        this.client.spriteBatch.setShader(null);
//
//        textures.put("ssao", this.getFrameBuffer().getColorBufferTexture());
        return input;
    }

    private void setUniforms() {
        // You might need to replace these values
        this.program.setUniformf("iResolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 1f);
        this.program.setUniformf("iTime", this.getTime());
        this.program.setUniformf("iTimeDelta", Gdx.graphics.getDeltaTime());
        this.program.setUniformf("iFrame", Gdx.graphics.getFrameId());
        this.program.setUniform1fv("iChannelTime", new float[]{this.getTime(), this.getTime(), this.getTime(), this.getTime()}, 0, 4);
        this.program.setUniformf("iMouse", Gdx.input.getX(), Gdx.input.getY(), Gdx.input.isButtonPressed(Input.Buttons.LEFT) ? 1 : 0, Gdx.input.isButtonPressed(Input.Buttons.RIGHT) ? 1 : 0); // Update this appropriately for your needs
        this.program.setUniformf("iDate", Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        this.program.setUniformf("iSampleRate", 48000f); // Most common sample rate is 48KHz, but replace this with your actual sample rate

        // Now pass the texture units to the iChannelX uniforms. The last parameter is the number of the texture unit.
        this.program.setUniform3fv("iChannelResolution", new float[]{this.tex0.getWidth(), this.tex0.getHeight(), 1, this.tex1.getWidth(), this.tex1.getHeight(), 1, this.tex2.getWidth(), this.tex2.getHeight(), 1, this.tex3.getWidth(), this.tex3.getHeight(), 1}, 0, 12);
        this.program.setUniformi("iChannel0", 0);
        this.program.setUniformi("iChannel1", 1);
        this.program.setUniformi("iChannel2", 2);
        this.program.setUniformi("iChannel3", 3);

        // Shader-specific uniforms.
        this.program.setUniformf("iGamma", 1.2f);
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

package com.ultreon.craft.client.render.shader;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.ultreon.craft.client.resources.ResourceFileHandle;

import static com.ultreon.craft.client.UltracraftClient.id;

public class MRTShader implements Shader {

    ShaderProgram shaderProgram;
    long attributes;

    RenderContext context;

    Matrix3 matrix3 = new Matrix3();

    public MRTShader(Renderable renderable) {
        String prefix = "";
        if (renderable.material.has(TextureAttribute.Diffuse)) {
            prefix += "#define texturedFlag\n";
        }

        String vert = new ResourceFileHandle(id("shaders/mrt.vert")).readString();
        String frag = new ResourceFileHandle(id("shaders/mrt.frag")).readString();
        this.shaderProgram = new ShaderProgram(prefix + vert, prefix + frag);
        if (!this.shaderProgram.isCompiled()) {
            throw new GdxRuntimeException(this.shaderProgram.getLog());
        }
        this.attributes = renderable.material.getMask();
    }

    @Override
    public void init() {
    }

    @Override
    public int compareTo(Shader other) {
        // quick and dirty shader sort
        if (((MRTShader) other).attributes == this.attributes) return 0;
        if ((((MRTShader) other).attributes & TextureAttribute.Normal) == 1) return -1;
        return 1;

    }

    @Override
    public boolean canRender(Renderable instance) {
        return this.attributes == instance.material.getMask();
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.context = context;
        this.shaderProgram.bind();
        this.shaderProgram.setUniformMatrix("u_projViewTrans", camera.combined);
        context.setDepthTest(GL20.GL_LEQUAL);
        context.setCullFace(GL20.GL_BACK);
    }

    @Override
    public void render(Renderable renderable) {
        Material material = renderable.material;

        TextureAttribute diffuseTexture = (TextureAttribute) material.get(TextureAttribute.Diffuse);
        TextureAttribute normalTexture = (TextureAttribute) material.get(TextureAttribute.Normal);
        TextureAttribute specTexture = (TextureAttribute) material.get(TextureAttribute.Specular);

        if (diffuseTexture != null) {
            this.shaderProgram.setUniformi("u_diffuseTexture", this.context.textureBinder.bind(diffuseTexture.textureDescription.texture));
        }
        if (normalTexture != null) {
            this.shaderProgram.setUniformi("u_normalTexture", this.context.textureBinder.bind(normalTexture.textureDescription.texture));
        }
        if (specTexture != null) {
            this.shaderProgram.setUniformi("u_specularTexture", this.context.textureBinder.bind(specTexture.textureDescription.texture));
        }

        this.shaderProgram.setUniformMatrix("u_worldTrans", renderable.worldTransform);
        this.shaderProgram.setUniformMatrix("u_normalMatrix", this.matrix3.set(renderable.worldTransform).inv().transpose());

        renderable.meshPart.render(this.shaderProgram);
    }

    @Override
    public void end() {

    }

    @Override
    public void dispose() {
        this.shaderProgram.dispose();
    }
}

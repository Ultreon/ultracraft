package com.ultreon.craft.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.ultreon.craft.client.init.ShaderPrograms;
import com.ultreon.craft.client.init.Shaders;
import com.ultreon.craft.client.input.GameCamera;
import com.ultreon.craft.util.Identifier;
import org.checkerframework.common.reflection.qual.NewInstance;

import java.util.function.Supplier;

public class SkyboxNode extends WorldRenderNode {
    public static final float SIZE = 1.0f;
    private final Mesh mesh;
    private final Supplier<ShaderProgram> shader;
    private Renderable renderable;

    public SkyboxNode() {
        super();

        MeshBuilder skybox = new MeshBuilder();
        skybox.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorUnpacked, GL20.GL_TRIANGLES);
        BoxShapeBuilder.build(skybox, SIZE*4, SIZE*4, SIZE*4, -SIZE, -SIZE, -SIZE);

        this.mesh = skybox.end();

        this.shader = getShaderProgram();

        Renderable renderable = new Renderable();
        renderable.material = new Material();
        renderable.meshPart.mesh = this.mesh;
        renderable.meshPart.size = this.mesh.getNumIndices() > 0 ? this.mesh.getNumIndices() : this.mesh.getNumVertices();
        renderable.meshPart.offset = 0;
        renderable.worldTransform.setToTranslationAndScaling(0, 0, 0, 1, 1, 1);
        renderable.userData = new Identifier("skybox");
        renderable.environment = this.client.getEnvironment();
//        renderable.shader = Shaders.SKYBOX.getShader(renderable);

        this.renderable = renderable;
    }

    private static Supplier<ShaderProgram> getShaderProgram() {
//        @Language("GLSL")
//        String vertexShader = """
//                attribute vec4 a_position;
//                varying vec3 v_position;
//                void main() {
//                  v_position = a_position.xyz;
//                  gl_Position = a_position;
//                }""";
//
//        @Language("GLSL")
//        String fragmentShader = """
//                #version 130
//                varying vec3 v_position;
//                void main() {
//                  vec3 color = normalize(v_position);
//                  gl_FragColor = vec4(color, 1.0);
//                }""";

        return ShaderPrograms.SKYBOX;
    }

    @NewInstance
    @Override
    public Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input) {
        Gdx.gl.glDepthMask(false);
        modelBatch.render(renderable);
        Gdx.gl.glDepthMask(true);

        textures.put("skybox", this.getFrameBuffer().getColorBufferTexture());
        return input;
    }

    @Override
    public void dispose() {
        super.dispose();

        this.mesh.dispose();
    }
}

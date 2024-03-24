package com.ultreon.craft.client.render.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Vector2;
import com.ultreon.craft.client.resources.ResourceFileHandle;

import static com.ultreon.craft.client.UltracraftClient.id;

public class Pass1Shader extends DepthShader {
    public int u_resolution;

    public Pass1Shader(Renderable renderable, Config config) {
        super(renderable, config, DefaultShader.createPrefix(renderable, config),
                new ResourceFileHandle(id("shaders/pass1.vert")).readString(), new ResourceFileHandle(id("shaders/pass1.frag")).readString());

        this.u_resolution = this.register(WorldInputs.resolution, WorldSetters.resolution);
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.set(this.u_resolution, new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        super.begin(camera, context);
    }

    private static class WorldSetters {
        public static final Setter resolution = new GlobalSetter() {
            @Override
            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
            }
        };
    }

    private static class WorldInputs {
        public static final Uniform resolution = new Uniform("u_resolution");
    }

    public static class Config extends DepthShader.Config {

    }
}

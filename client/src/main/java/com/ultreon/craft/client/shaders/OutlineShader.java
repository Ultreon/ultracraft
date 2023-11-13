package com.ultreon.craft.client.shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.ultreon.craft.client.init.Shaders;
import com.ultreon.craft.util.Color;

public class OutlineShader extends BaseShader {
    private final Color outlineColor;

    public OutlineShader(Color outlineColor) {
        this.outlineColor = outlineColor;
        this.program = Shaders.OUTLINE;
    }

    @Override
    public void init() {
        if (!this.program.isCompiled()) {
            throw new IllegalStateException("Outline shader is not compiled");
        }
    }

    @Override
    public int compareTo(Shader other) {
        if (other == null) return -1;
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        String id = instance.meshPart.id;
        if (id == null) return false;
        return id.startsWith("ultracraft:outline_");
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        super.begin(camera, context);

        this.program.setAttributef("a_position", 0.0f, 0.0f, 0.0f, 0.0f);
        this.program.setUniformMatrix4fv("u_projTrans", camera.combined.val, 0, camera.combined.val.length);
        this.program.setUniformf("u_outlineColor", this.outlineColor.toGdx());
        this.program.bind();
    }
}

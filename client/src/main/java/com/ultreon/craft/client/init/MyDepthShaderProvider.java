package com.ultreon.craft.client.init;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.ultreon.craft.client.render.shader.OpenShaderProvider;

class MyDepthShaderProvider extends DepthShaderProvider implements OpenShaderProvider {
    public MyDepthShaderProvider(DepthShader.Config config) {
        super(config);
    }

    public MyDepthShaderProvider(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public MyDepthShaderProvider(FileHandle vertexShader, FileHandle fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public MyDepthShaderProvider() {
        super();
    }

    @Override
    public Shader createShader(Renderable renderable) {
        DepthShader depthShader = new DepthShader(renderable, this.config) {
            @Override
            public boolean canRender(Renderable r) {
//                    if (r != renderable) LOGGER.warn("Requested renderable does not equal to the original renderable.");
//                    if (r.shader == null && renderable.shader == null) return super.canRender(renderable);
//                    if (r.shader != renderable.shader) {
//                        LOGGER.warn("Identity error: " + r.shader + " is not " + renderable.shader);
//                        return false;
//                    }
                return super.canRender(r);
            }

            @Override
            public String toString() {
                return String.valueOf(this.program);
            }
        };
        Shaders.checkShaderCompilation(depthShader.program);

        return depthShader;
    }


}

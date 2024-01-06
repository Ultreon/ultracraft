package com.ultreon.craft.client.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.client.ClientRegistries;
import com.ultreon.craft.util.ElementID;
import org.jetbrains.annotations.ApiStatus;

public class RenderType {

    public static final RenderType DEFAULT = RenderType.register("default", new RenderType());
    public static final RenderType WATER = RenderType.register("water", new RenderType());

    private static RenderType register(String name, RenderType renderType) {
        ClientRegistries.RENDER_TYPE.register(new ElementID(CommonConstants.NAMESPACE, name), renderType);
        return renderType;
    }

    public void nopInit() {
        // Load class
    }

    @ApiStatus.Experimental
    public Shader getShader(Renderable renderable) {
        return new DefaultShader(renderable);
    }
}

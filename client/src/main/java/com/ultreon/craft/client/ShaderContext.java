package com.ultreon.craft.client;

import com.ultreon.craft.client.render.shader.OpenShaderProvider;

public class ShaderContext {
    private static OpenShaderProvider mode;

    public static void set(OpenShaderProvider mode) {
        ShaderContext.mode = mode;
    }

    public static OpenShaderProvider get() {
        return ShaderContext.mode;
    }

    public enum ShaderMode {
        DEPTH,
        DIFFUSE
    }
}

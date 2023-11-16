package com.ultreon.craft.client.init;

import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.resources.ResourceFileHandle;
import com.ultreon.libs.registries.v0.Registry;

import static com.ultreon.craft.client.UltracraftClient.id;

public class Shaders {
    public static final Registry<ShaderProvider> REGISTRY = Registry.create(id("shaders"));

    public static final DepthShaderProvider DEPTH = Shaders.register("depth", new MyDepthShaderProvider());

    public static final DefaultShaderProvider DEFAULT = Shaders.register("default", new MyDefaultShaderProvider());

    private static <T extends ShaderProvider> T register(String name, T provider) {
        Shaders.REGISTRY.register(id(name), provider);
        return provider;
    }

    private static DepthShaderProvider registerDepth(String name, ShaderProgram program) {
        DepthShaderProvider provider = new MyDepthShaderProvider(
                new ResourceFileHandle(id(name).mapPath(s -> "shaders/" + s + ".vert")),
                new ResourceFileHandle(id(name).mapPath(s -> "shaders/" + s + ".frag")));
        Shaders.REGISTRY.register(UltracraftClient.id(name), provider);
        return provider;
    }

    private static DefaultShaderProvider registerDefault(String name, ShaderProgram program) {
        DefaultShaderProvider provider = new MyDefaultShaderProvider(
                new ResourceFileHandle(id(name).mapPath(s -> "shaders/" + s + ".vert")),
                new ResourceFileHandle(id(name).mapPath(s -> "shaders/" + s + ".frag")));
        Shaders.REGISTRY.register(UltracraftClient.id(name), provider);
        return provider;
    }

    protected static void checkShaderCompilation(ShaderProgram program) {
        String shaderLog = program.getLog();
        if (program.isCompiled()) {
            if (shaderLog.isEmpty()) UltracraftClient.LOGGER.debug("Shader compilation success");
            else UltracraftClient.LOGGER.warn("Shader compilation warnings:\n{}", shaderLog);
        } else throw new GdxRuntimeException("Shader compilation failed:\n" + shaderLog);
    }

    public static void nopInit() {
        // NOOP
    }
}

package com.ultreon.craft.client.init;

import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.resources.ResourceFileHandle;
import com.ultreon.craft.client.shaders.provider.ModelViewShaderProvider;
import com.ultreon.craft.client.shaders.provider.WorldShaderProvider;
import com.ultreon.craft.registry.Registry;

import static com.ultreon.craft.client.UltracraftClient.id;

public class Shaders {
    public static final Registry<ShaderProvider> REGISTRY = Registry.create(id("shaders"));

    public static final DepthShaderProvider DEPTH = Shaders.registerDepth("depth");

    public static final DefaultShaderProvider DEFAULT = Shaders.register("default", new MyDefaultShaderProvider());
    public static final DefaultShaderProvider WORLD = Shaders.register("world", new WorldShaderProvider(
            new ResourceFileHandle(UltracraftClient.id("shaders/world.vert")),
            new ResourceFileHandle(UltracraftClient.id("shaders/world.frag"))
    ));
    public static final DefaultShaderProvider MODEL_VIEW = Shaders.register("model_view", new ModelViewShaderProvider(
            new ResourceFileHandle(UltracraftClient.id("shaders/model_view.vert")),
            new ResourceFileHandle(UltracraftClient.id("shaders/model_view.frag"))
    ));

    private static <T extends ShaderProvider> T register(String name, T provider) {
        Shaders.REGISTRY.register(id(name), provider);
        return provider;
    }

    private static DepthShaderProvider registerDepth(String name) {
        DepthShaderProvider provider = new MyDepthShaderProvider(
                new ResourceFileHandle(id(name).mapPath(s -> "shaders/" + name + ".vert")),
                new ResourceFileHandle(id(name).mapPath(s -> "shaders/" + name + ".frag")));
        Shaders.REGISTRY.register(UltracraftClient.id(name), provider);
        return provider;
    }

    private static DefaultShaderProvider registerDefault(String name) {
        DefaultShaderProvider provider = new MyDefaultShaderProvider(
                new ResourceFileHandle(id(name).mapPath(s -> "shaders/" + name + ".vert")),
                new ResourceFileHandle(id(name).mapPath(s -> "shaders/" + name + ".frag")));
        Shaders.REGISTRY.register(UltracraftClient.id(name), provider);
        return provider;
    }

    public static void checkShaderCompilation(ShaderProgram program, String filename) {
        String shaderLog = program.getLog();
        if (program.isCompiled()) {
            if (shaderLog.isEmpty()) UltracraftClient.LOGGER.debug("Shader compilation for {} success", filename);
            else UltracraftClient.LOGGER.warn("Shader compilation warnings for {}:\n{}", filename, shaderLog);
        } else throw new GdxRuntimeException("Shader compilation failed for " + filename + ":\n" + shaderLog);
    }

    public static void nopInit() {
        // NOOP
    }
}

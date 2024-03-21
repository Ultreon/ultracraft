package com.ultreon.craft.client.init;

import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.common.base.Supplier;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.resources.ResourceFileHandle;
import com.ultreon.craft.client.shaders.provider.ModelViewShaderProvider;
import com.ultreon.craft.client.shaders.provider.SkyboxShaderProvider;
import com.ultreon.craft.client.shaders.provider.WorldShaderProvider;

import static com.ultreon.craft.client.UltracraftClient.get;
import static com.ultreon.craft.client.UltracraftClient.id;

@SuppressWarnings("SameParameterValue")
public class Shaders {
    public static final Supplier<DepthShaderProvider> DEPTH = Shaders.register("depth", () -> new MyDepthShaderProvider(
            new ResourceFileHandle(id("shaders/depth.vert")),
            new ResourceFileHandle(id("shaders/depth.frag"))
    ));

    public static final Supplier<DefaultShaderProvider> DEFAULT = Shaders.register("default", MyDefaultShaderProvider::new);
    
    public static final Supplier<WorldShaderProvider> WORLD = Shaders.register("world", () -> new WorldShaderProvider(
            new ResourceFileHandle(id("shaders/world.vert")),
            new ResourceFileHandle(id("shaders/world.frag"))
    ));
    public static final Supplier<DefaultShaderProvider> SKYBOX = Shaders.register("skybox", () -> new SkyboxShaderProvider(
            new ResourceFileHandle(id("shaders/skybox.vert")),
            new ResourceFileHandle(id("shaders/skybox.frag"))
    ));
    public static final Supplier<ModelViewShaderProvider> MODEL_VIEW = Shaders.register("model_view", () -> new ModelViewShaderProvider(
            new ResourceFileHandle(id("shaders/model_view.vert")),
            new ResourceFileHandle(id("shaders/model_view.frag"))
    ));

    private static <T extends ShaderProvider> Supplier<T> register(String name, Supplier<T> provider) {
        return get().getShaderProviderManager().register(id(name), provider);
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

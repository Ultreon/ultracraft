package com.ultreon.craft.client.init;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.resources.ResourceFileHandle;
import com.ultreon.craft.util.Identifier;

import java.util.function.Supplier;

public class ShaderPrograms {
    public static final Supplier<ShaderProgram> XOR = ShaderPrograms.register("xor");
    public static final Supplier<ShaderProgram> OUTLINE = ShaderPrograms.register("outline");
    public static final Supplier<ShaderProgram> MODEL = ShaderPrograms.register("model");
    public static final Supplier<ShaderProgram> DEFAULT = ShaderPrograms.register("default");
    public static final Supplier<ShaderProgram> DEPTH = ShaderPrograms.register("depth");
    public static final Supplier<ShaderProgram> WORLD = ShaderPrograms.register("world");
    public static final Supplier<ShaderProgram> SKYBOX = ShaderPrograms.register("skybox");

    private static Supplier<ShaderProgram> register(String name) {
        return UltracraftClient.get().getShaderProgramManager().register(UltracraftClient.id(name), () -> {
            Identifier id = UltracraftClient.id(name);

            return ShaderPrograms.createShader(id);
        });
    }

    public static ShaderProgram createShader(Identifier id) {
        ResourceFileHandle vertexResource = new ResourceFileHandle(id.mapPath(s -> "shaders/" + s + ".vert"));
        ResourceFileHandle fragmentResource = new ResourceFileHandle(id.mapPath(s -> "shaders/" + s + ".frag"));

        ShaderProgram program = new ShaderProgram(vertexResource, fragmentResource);
        String shaderLog = program.getLog();
        if (program.isCompiled()) {
            if (shaderLog.isEmpty()) UltracraftClient.LOGGER.debug("Shader compilation success");
            else UltracraftClient.LOGGER.warn("Shader compilation warnings:\n{}", shaderLog);
        } else throw new GdxRuntimeException("Shader compilation failed:\n" + shaderLog);
        return program;
    }

    public static void nopInit() {
        // NOOP
    }
}

package com.ultreon.craft.client.init;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.resources.ResourceFileHandle;
import com.ultreon.craft.registry.Registry;
import com.ultreon.craft.util.Identifier;

import static com.ultreon.craft.client.UltracraftClient.id;

public class ShaderPrograms {
    public static final Registry<ShaderProgram> REGISTRY = Registry.create(id("shader_programs"));

    public static final ShaderProgram XOR = ShaderPrograms.register("xor");
    public static final ShaderProgram OUTLINE = ShaderPrograms.register("outline");
    public static final ShaderProgram MODEL = ShaderPrograms.register("model");
    public static final ShaderProgram DEFAULT = ShaderPrograms.register("default");
    public static final ShaderProgram DEPTH = ShaderPrograms.register("depth");
    public static final ShaderProgram WORLD = ShaderPrograms.register("world");

    private static ShaderProgram register(String name) {
        Identifier id = UltracraftClient.id(name);
        ShaderProgram shader = ShaderPrograms.createShader(id);
        ShaderPrograms.REGISTRY.register(id, shader);
        return shader;
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

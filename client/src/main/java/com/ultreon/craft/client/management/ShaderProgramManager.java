package com.ultreon.craft.client.management;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.resources.ReloadContext;
import com.ultreon.craft.client.resources.ResourceFileHandle;
import com.ultreon.craft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ShaderProgramManager implements Manager<ShaderProgram> {
    private final Map<Identifier, ShaderProgram> programs = new LinkedHashMap<>();
    private final Map<Identifier, Supplier<ShaderProgram>> factories = new LinkedHashMap<>();

    @Override
    public ShaderProgram register(@NotNull Identifier id, @NotNull ShaderProgram program) {
        this.programs.put(id, program);
        return program;
    }

    public Supplier<ShaderProgram> register(Identifier id, Supplier<ShaderProgram> factory) {
        Supplier<ShaderProgram> supplier = () -> {
            if (this.programs.containsKey(id))
                return this.programs.get(id);

            ShaderProgram program = factory.get();
            this.programs.put(id, program);
            return program;
        };

        this.factories.put(id, supplier);
        return supplier;
    }

    @Override
    public @Nullable ShaderProgram get(Identifier id) {
        ShaderProgram program = this.programs.get(id);

        if (program != null)
            return program;

        ShaderProgram shaderProgram = new ShaderProgram(
                new ResourceFileHandle(id.mapPath(p -> "shaders/" + p + ".vert")),
                new ResourceFileHandle(id.mapPath(p -> "shaders/" + p + ".frag"))
        );

        if (!shaderProgram.isCompiled())
            throw new RuntimeException("Failed to compile shader program: " + shaderProgram.getLog());

        if (!shaderProgram.getLog().isEmpty())
            UltracraftClient.LOGGER.warn("Warning while compiling shader program: {}", shaderProgram.getLog());

        this.register(id, shaderProgram);
        program = shaderProgram;

        return program;
    }

    @Override
    public void reload(ReloadContext context) {
        for (ShaderProgram shader : List.copyOf(this.programs.values())) {
            context.submit(shader::dispose);
        }

        this.programs.clear();
    }
}

package com.ultreon.craft.init;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.resources.ResourceFileHandle;
import com.ultreon.libs.commons.v0.Identifier;

public class Shaders {
    public static final ShaderProgram XOR = register("xor");

    private static ShaderProgram register(String name) {
        Identifier id = UltreonCraft.id(name);
        ShaderProgram shader = createShader(id);
        Registries.SHADERS.register(id, shader);
        return shader;
    }

    public static ShaderProgram createShader(Identifier id) {
        ResourceFileHandle vertexResource = new ResourceFileHandle(id.mapPath(s -> "shaders/" + s + ".vert"));
        ResourceFileHandle fragmentResource = new ResourceFileHandle(id.mapPath(s -> "shaders/" + s + ".frag"));

        return new ShaderProgram(vertexResource, fragmentResource);
    }
}

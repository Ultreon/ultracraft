package com.ultreon.craft.client.init;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.resources.ResourceFileHandle;
import com.ultreon.libs.commons.v0.Identifier;

public class Shaders {
    public static final ShaderProgram XOR = Shaders.register("xor");

    private static ShaderProgram register(String name) {
        Identifier id = UltracraftClient.id(name);
        return Shaders.createShader(id);
    }

    public static ShaderProgram createShader(Identifier id) {
        ResourceFileHandle vertexResource = new ResourceFileHandle(id.mapPath(s -> "shaders/" + s + ".vert"));
        ResourceFileHandle fragmentResource = new ResourceFileHandle(id.mapPath(s -> "shaders/" + s + ".frag"));

        return new ShaderProgram(vertexResource, fragmentResource);
    }
}

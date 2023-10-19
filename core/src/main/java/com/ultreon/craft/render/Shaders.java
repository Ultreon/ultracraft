package com.ultreon.craft.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.libs.commons.v0.Identifier;

@SuppressWarnings("GDXJavaStaticResource")
public class Shaders {
    // Create a new shader that modifies the texture coordinates
    public static final ShaderProgram MINING = create(UltreonCraft.id("mining"));

    private static ShaderProgram create(Identifier id) {
        return new ShaderProgram(
                Gdx.files.internal("assets/" + id.location() + "/shaders/" + id.path() + ".vert"),
                Gdx.files.internal("assets/" + id.location() + "/shaders/" + id.path() + ".frag")
        );
    }

    public static void nopInit() {

    }
}

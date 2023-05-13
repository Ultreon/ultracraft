package com.ultreon.craft;

import com.badlogic.gdx.graphics.Texture;
import com.google.common.base.Preconditions;
import com.ultreon.libs.commons.v0.Identifier;

import java.util.HashMap;
import java.util.Map;

public class TextureManager {
    private final Map<Identifier, Texture> cache = new HashMap<>();

    public Texture getTexture(Identifier id) {
        if (!cache.containsKey(id)) {
            registerTexture(id);
        }
        return Preconditions.checkNotNull(cache.get(id), "Texture not registered");
    }

    public Texture registerTexture(Identifier id) {
        Texture tex = new Texture("assets/" + id.location() + "/" + id.path());
        cache.put(id, tex);
        return tex;
    }
}

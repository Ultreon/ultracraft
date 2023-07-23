package com.ultreon.craft;

import com.badlogic.gdx.graphics.Texture;
import com.google.common.base.Preconditions;
import com.ultreon.libs.commons.v0.Identifier;

import java.util.HashMap;
import java.util.Map;

import static com.ultreon.craft.UltreonCraft.id;

public class TextureManager {
    private final Map<Identifier, Texture> cache = new HashMap<>();

    public Texture getTexture(Identifier id) {
        if (!this.cache.containsKey(id)) {
            if (UltreonCraft.isOnMainThread()) {
                return this.registerTexture(id);
            } else {
                return UltreonCraft.get().getAndWait(id("texture_manager/register_texture"), () -> this.registerTexture(id));
            }
        }
        return Preconditions.checkNotNull(this.cache.get(id), "Texture not registered");
    }

    public Texture registerTexture(Identifier id) {
        Texture tex = new Texture("assets/" + id.location() + "/" + id.path());
        this.cache.put(id, tex);
        return tex;
    }
}

package com.ultreon.craft;

import com.badlogic.gdx.graphics.Texture;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.resources.v0.ResourceManager;

import java.util.HashMap;
import java.util.Map;

public class TextureManager {
    private final Map<Identifier, Texture> textures = new HashMap<>();
    private final ResourceManager resourceManager;

    public TextureManager(ResourceManager resourceManager) {
        Preconditions.checkNotNull(resourceManager, "resourceManager");

        this.resourceManager = resourceManager;
    }

    public Texture getTexture(Identifier id) {
        Preconditions.checkNotNull(id, "id");

        if (!this.textures.containsKey(id)) return this.registerTexture(id);

        Texture texture = this.textures.get(id);
        if (texture == null) throw new IllegalStateException("Texture isn't registered.");
        return texture;
    }

    @CanIgnoreReturnValue
    public Texture registerTexture(Identifier id) {
        Preconditions.checkNotNull(id, "id");
        Texture oldTexture = this.textures.get(id);
        if (oldTexture != null) return oldTexture;

        Texture texture = new Texture(UltreonCraft.resource(id));
        this.textures.put(id, texture);
        return texture;
    }

    @CanIgnoreReturnValue
    public Texture registerTexture(Identifier id, Texture texture) {
        Preconditions.checkNotNull(id, "id");
        Preconditions.checkNotNull(texture, "texture");

        if (this.textures.containsKey(id)) throw new IllegalArgumentException("A texture is already registered with id: " + id);
        this.textures.put(id, texture);
        return texture;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }
}

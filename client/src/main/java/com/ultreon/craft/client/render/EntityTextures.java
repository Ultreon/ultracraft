package com.ultreon.craft.client.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.util.Identifier;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import org.checkerframework.common.returnsreceiver.qual.This;

public class EntityTextures {
    private final Long2ObjectMap<Texture> textureMap = new Long2ObjectArrayMap<>();

    public @This EntityTextures set(long attribute, Identifier texture) {
        this.textureMap.put(attribute, UltracraftClient.get().getTextureManager().getTexture(texture));
        return this;
    }

    public Texture get(long attribute) {
        return this.textureMap.get(attribute);
    }

    public Long2ObjectMap<Texture> getTextureMap() {
        return Long2ObjectMaps.unmodifiable(this.textureMap);
    }

    public Material createMaterial() {
        Material material = new Material();
        for (var e : this.textureMap.long2ObjectEntrySet()) {
            material.set(new TextureAttribute(e.getLongKey(), e.getValue()));
        }
        return material;
    }
}

package com.ultreon.craft.client.atlas;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.client.util.TextureOffset;
import com.ultreon.libs.commons.v0.Identifier;

import java.util.Map;

public class TextureAtlas implements Disposable {
    private final TextureStitcher stitcher;
    private final Texture atlas;
    private final Map<Identifier, TextureOffset> uvMap;

    public TextureAtlas(TextureStitcher stitcher, Texture atlas, Map<Identifier, TextureOffset> uvMap) {
        this.stitcher = stitcher;
        this.atlas = atlas;
        this.uvMap = uvMap;
    }

    public TextureRegion get(Identifier id) {
        if (id == null) return null;
        TextureOffset textureOffset = this.uvMap.get(id);
        if (textureOffset == null) return null;
        return new TextureRegion(this.atlas, textureOffset.u(), textureOffset.v(), textureOffset.uWidth(), textureOffset.vHeight());
    }

    public Texture getTexture() {
        return this.atlas;
    }

    public void dispose() {
        this.stitcher.dispose();
    }
}
package com.ultreon.craft.client.atlas;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.client.util.TextureOffset;
import com.ultreon.craft.util.ElementID;

import java.util.Map;

public class TextureAtlas implements Disposable {
    private final TextureStitcher stitcher;
    private final Texture atlas;
    private final Texture emissiveAtlas;
    private final Map<ElementID, TextureOffset> uvMap;

    public TextureAtlas(TextureStitcher stitcher, Texture atlas, Texture emissiveAtlas, Map<ElementID, TextureOffset> uvMap) {
        this.stitcher = stitcher;
        this.atlas = atlas;
        this.emissiveAtlas = emissiveAtlas;
        this.uvMap = uvMap;
    }

    public TextureRegion get(ElementID id) {
        if (id == null) return null;
        TextureOffset textureOffset = this.uvMap.get(id);
        if (textureOffset == null) return null;
        TextureRegion textureRegion = new TextureRegion(this.atlas, textureOffset.u(), textureOffset.v(), textureOffset.uWidth(), textureOffset.vHeight());
        textureRegion.flip(false, true);
        return textureRegion;
    }

    public TextureRegion getEmissive(ElementID id) {
        if (id == null) return null;
        TextureOffset textureOffset = this.uvMap.get(id);
        if (textureOffset == null) return null;
        TextureRegion textureRegion = new TextureRegion(this.emissiveAtlas, textureOffset.u(), textureOffset.v(), textureOffset.uWidth(), textureOffset.vHeight());
        textureRegion.flip(false, true);
        return textureRegion;
    }

    public Texture getTexture() {
        return this.atlas;
    }

    public Texture getEmissiveTexture() {
        return this.emissiveAtlas;
    }

    @Override
    public void dispose() {
        this.stitcher.dispose();
    }
}

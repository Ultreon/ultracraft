package com.ultreon.craft.render.texture.atlas;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.ultreon.craft.TextureManager;
import com.ultreon.craft.render.UV;
import com.ultreon.libs.commons.v0.Identifier;

import java.util.Map;

public class TextureAtlas {
    private final TextureStitcher stitcher;
    private final Texture textureAtlas;
    private final Map<Identifier, UV> uvMap;

    public TextureAtlas(TextureStitcher stitcher,Texture textureAtlas, Map<Identifier, UV> uvMap) {
        this.stitcher = stitcher;
        this.textureAtlas = textureAtlas;
        this.uvMap = uvMap;
    }

    public TextureRegion get(Identifier id) {
        if (id == null) return null;
        var uv = this.uvMap.get(id);
        if (uv == null) return null;
        return new TextureRegion(this.textureAtlas, uv.u(), uv.v(), uv.uWidth(), uv.vHeight());
    }

    public Texture getTexture() {
        return this.textureAtlas;
    }

    public void dispose() {
        this.stitcher.dispose();
    }
}

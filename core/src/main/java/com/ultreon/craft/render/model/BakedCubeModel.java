package com.ultreon.craft.render.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.ultreon.craft.TextureManager;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.libs.commons.v0.Identifier;

public record BakedCubeModel(TextureRegion top, TextureRegion bottom,
                             TextureRegion left, TextureRegion right,
                             TextureRegion front, TextureRegion back) {
    public static final BakedCubeModel DEFAULT;

    static {
        TextureRegion missingno = UltreonCraft.get().blocksTextureAtlas.get(new Identifier("missingno"));
        DEFAULT = new BakedCubeModel(
                missingno, missingno,
                missingno, missingno,
                missingno, missingno
        );
    }
}

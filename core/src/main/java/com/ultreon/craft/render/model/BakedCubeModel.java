package com.ultreon.craft.render.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.ultreon.craft.render.UV;

public record BakedCubeModel(TextureRegion top, TextureRegion bottom,
                             TextureRegion left, TextureRegion right,
                             TextureRegion front, TextureRegion back) {

    public BakedCubeModel(TextureRegion top, TextureRegion bottom, TextureRegion side) {
        this(top, bottom, side, side, side, side);
    }

    public BakedCubeModel(TextureRegion top, TextureRegion bottom, TextureRegion front, TextureRegion side) {
        this(top, bottom, side, side, front, side);
    }

    public BakedCubeModel(TextureRegion top, TextureRegion bottom, TextureRegion front, TextureRegion back, TextureRegion side) {
        this(top, bottom, side, side, front, back);
    }
}

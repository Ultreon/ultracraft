package com.ultreon.craft.render.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public record BakedCubeModel(TextureRegion top, TextureRegion bottom,
                             TextureRegion left, TextureRegion right,
                             TextureRegion front, TextureRegion back) {
}

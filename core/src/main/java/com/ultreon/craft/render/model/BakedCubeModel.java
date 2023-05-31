package com.ultreon.craft.render.model;

import com.badlogic.gdx.graphics.Texture;

public record BakedCubeModel(Texture top, Texture bottom,
                             Texture left, Texture right,
                             Texture front, Texture back) {
}

package com.ultreon.craft.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public record UV(int u, int v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
    public UV(int u, int v) {
        this(u, v, 16, 16);
    }

    public UV(int u, int v, int uWidth, int vHeight) {
        this(u, v, uWidth, vHeight, 256, 256);
    }

    public static UV blockUV(int u, int v) {
        return new UV(u, v, 1, 1, 16, 16);
    }

    public TextureRegion bake(Texture texture) {
        return new TextureRegion(texture,
                (float)u / (float)textureWidth, (float)v / (float)textureHeight,
                (float)(u + uWidth) / (float)textureWidth, (float)(v + vHeight) / (float)textureHeight);
    }
}

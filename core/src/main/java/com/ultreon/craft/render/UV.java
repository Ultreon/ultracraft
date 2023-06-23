package com.ultreon.craft.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Objects;

public final class UV {
    private final int u;
    private final int v;
    private final int uWidth;
    private final int vHeight;
    private final int textureWidth;
    private final int textureHeight;

    public UV(int u, int v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        this.u = u;
        this.v = v;
        this.uWidth = uWidth;
        this.vHeight = vHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public int u() {
        return this.u;
    }

    public int v() {
        return this.v;
    }

    public int uWidth() {
        return this.uWidth;
    }

    public int vHeight() {
        return this.vHeight;
    }

    public int textureWidth() {
        return this.textureWidth;
    }

    public int textureHeight() {
        return this.textureHeight;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UV) obj;
        return this.u == that.u &&
                this.v == that.v &&
                this.uWidth == that.uWidth &&
                this.vHeight == that.vHeight &&
                this.textureWidth == that.textureWidth &&
                this.textureHeight == that.textureHeight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.u, this.v, this.uWidth, this.vHeight, this.textureWidth, this.textureHeight);
    }

    @Override
    public String toString() {
        return "UV[" +
                "u=" + this.u + ", " +
                "v=" + this.v + ", " +
                "uWidth=" + this.uWidth + ", " +
                "vHeight=" + this.vHeight + ", " +
                "textureWidth=" + this.textureWidth + ", " +
                "textureHeight=" + this.textureHeight + ']';
    }

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
                (float) this.u / (float) this.textureWidth, (float) this.v / (float) this.textureHeight,
                (float) (this.u + this.uWidth) / (float) this.textureWidth, (float) (this.v + this.vHeight) / (float) this.textureHeight);
    }
}

package com.ultreon.craft.client.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Objects;

/**
 * Represents an offset in a texture.
 *
 * @since 0.1.0
 * @see Texture
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public final class TextureOffset {
    private final int u;
    private final int v;
    private final int uWidth;
    private final int vHeight;
    private final int textureWidth;
    private final int textureHeight;


    /**
     * Constructs a TextureOffset with all parameters.
     *
     * @param u The U coordinate.
     * @param v The V coordinate.
     * @param uWidth The width in U.
     * @param vHeight The height in V.
     * @param textureWidth The width of the texture.
     * @param textureHeight The height of the texture.
     */
    public TextureOffset(int u, int v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        this.u = u;
        this.v = v;
        this.uWidth = uWidth;
        this.vHeight = vHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    /**
     * Constructs a TextureOffset with default textureWidth and textureHeight.
     *
     * @param u The U coordinate.
     * @param v The V coordinate.
     */
    public TextureOffset(int u, int v) {
        this(u, v, 16, 16);
    }

    /**
     * Constructs a TextureOffset with default textureWidth and textureHeight, and specified uWidth and vHeight.
     *
     * @param u The U coordinate.
     * @param v The V coordinate.
     * @param uWidth The width in U.
     * @param vHeight The height in V.
     */
    public TextureOffset(int u, int v, int uWidth, int vHeight) {
        this(u, v, uWidth, vHeight, 256, 256);
    }
    /**
     * Get the texture U.
     * @return the texture U
     */
    public int u() {
        return this.u;
    }

    /**
     * Get the texture V.
     * @return the texture V
     */
    public int v() {
        return this.v;
    }

    /**
     * Get the texture UV-width.
     * @return the texture UV-width
     */
    public int uWidth() {
        return this.uWidth;
    }

    /**
     * Get the texture UV-height.
     * @return the texture UV-height
     */
    public int vHeight() {
        return this.vHeight;
    }

    /**
     * Get the texture width.
     * @return the texture width
     */
    public int textureWidth() {
        return this.textureWidth;
    }

    /**
     * Get the texture height.
     * @return the texture height
     */
    public int textureHeight() {
        return this.textureHeight;
    }

    /**
     * Checks if this TextureOffset is equal to another object.
     *
     * @param obj the object to compare to
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        TextureOffset that = (TextureOffset) obj;
        return this.u == that.u &&
                this.v == that.v &&
                this.uWidth == that.uWidth &&
                this.vHeight == that.vHeight &&
                this.textureWidth == that.textureWidth &&
                this.textureHeight == that.textureHeight;
    }

    /**
     * Generates a hash code for this TextureOffset based on its attributes.
     *
     * @return the generated hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.u, this.v, this.uWidth, this.vHeight, this.textureWidth, this.textureHeight);
    }

    /**
     * Returns a string representation of this TextureOffset.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "TextureOffset[" +
                "u=" + this.u + ", " +
                "v=" + this.v + ", " +
                "uWidth=" + this.uWidth + ", " +
                "vHeight=" + this.vHeight + ", " +
                "textureWidth=" + this.textureWidth + ", " +
                "textureHeight=" + this.textureHeight + ']';
    }

    /**
     * Creates a new TextureOffset with specified u and v values.
     *
     * @param u the u value
     * @param v the v value
     * @return the new TextureOffset
     */
    public static TextureOffset blockUV(int u, int v) {
        return new TextureOffset(u, v, 1, 1, 16, 16);
    }

    /**
     * Creates a TextureRegion based on this TextureOffset and a given Texture.
     *
     * @param texture the Texture to create the TextureRegion from
     * @return the generated TextureRegion
     */
    public TextureRegion bake(Texture texture) {
        return new TextureRegion(texture,
                (float) this.u / (float) this.textureWidth, (float) this.v / (float) this.textureHeight,
                (float) (this.u + this.uWidth) / (float) this.textureWidth, (float) (this.v + this.vHeight) / (float) this.textureHeight);
    }
}

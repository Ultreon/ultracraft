package com.ultreon.craft.render.model;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Objects;

public final class BakedCubeModel {
    private final TextureRegion top;
    private final TextureRegion bottom;
    private final TextureRegion left;
    private final TextureRegion right;
    private final TextureRegion front;
    private final TextureRegion back;

    public BakedCubeModel(TextureRegion top, TextureRegion bottom,
                          TextureRegion left, TextureRegion right,
                          TextureRegion front, TextureRegion back) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.front = front;
        this.back = back;
    }

    public TextureRegion top() {
        return this.top;
    }

    public TextureRegion bottom() {
        return this.bottom;
    }

    public TextureRegion left() {
        return this.left;
    }

    public TextureRegion right() {
        return this.right;
    }

    public TextureRegion front() {
        return this.front;
    }

    public TextureRegion back() {
        return this.back;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BakedCubeModel) obj;
        return Objects.equals(this.top, that.top) &&
                Objects.equals(this.bottom, that.bottom) &&
                Objects.equals(this.left, that.left) &&
                Objects.equals(this.right, that.right) &&
                Objects.equals(this.front, that.front) &&
                Objects.equals(this.back, that.back);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.top, this.bottom, this.left, this.right, this.front, this.back);
    }

    @Override
    public String toString() {
        return "BakedCubeModel[" +
                "top=" + this.top + ", " +
                "bottom=" + this.bottom + ", " +
                "left=" + this.left + ", " +
                "right=" + this.right + ", " +
                "front=" + this.front + ", " +
                "back=" + this.back + ']';
    }

}

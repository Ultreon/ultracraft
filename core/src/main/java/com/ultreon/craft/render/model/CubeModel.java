package com.ultreon.craft.render.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.ultreon.craft.render.texture.atlas.TextureAtlas;
import com.ultreon.libs.commons.v0.Identifier;

import java.util.Objects;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ReferenceArraySet;

public final class CubeModel {
    private final Identifier top;
    private final Identifier bottom;
    private final Identifier left;
    private final Identifier right;
    private final Identifier front;
    private final Identifier back;

    private CubeModel(Identifier top, Identifier bottom,
                      Identifier left, Identifier right,
                      Identifier front, Identifier back) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.front = front;
        this.back = back;
    }

    public static CubeModel of(Identifier all) {
        return of(all, all, all);
    }

    public static CubeModel of(Identifier vertical,  Identifier side) {
        return of(vertical, vertical, side);
    }

    public static CubeModel of(Identifier top, Identifier bottom, Identifier side) {
        return of(top, bottom, side, side, side, side);
    }

    public static CubeModel of(Identifier top, Identifier bottom, Identifier side, Identifier front) {
        return of(top, bottom, side, side, front, side);
    }

    public static CubeModel of(Identifier top, Identifier bottom, Identifier side, Identifier front, Identifier back) {
        return of(top, bottom, side, side, front, back);
    }

    public static CubeModel of(Identifier top, Identifier bottom, Identifier left, Identifier right, Identifier front, Identifier back) {
        return new CubeModel(top, bottom, left, right, front, back);
    }

    @Deprecated
    public BakedCubeModel bake(Texture texture) {
//        return new BakedCubeModel(
//                top.bake(texture), bottom.bake(texture),
//                left.bake(texture), right.bake(texture),
//                front.bake(texture), back.bake(texture)
//        );
        return null;
    }

    public BakedCubeModel bake(TextureAtlas texture) {
        TextureRegion topTex = texture.get(this.top);
        TextureRegion bottomTex = texture.get(this.bottom);
        TextureRegion leftTex = texture.get(this.left);
        TextureRegion rightTex = texture.get(this.right);
        TextureRegion frontTex = texture.get(this.front);
        TextureRegion backTex = texture.get(this.back);
        return new BakedCubeModel(
                topTex, bottomTex,
                leftTex, rightTex,
                frontTex, backTex
        );
    }

    public Identifier top() {
        return top;
    }

    public Identifier bottom() {
        return bottom;
    }

    public Identifier left() {
        return left;
    }

    public Identifier right() {
        return right;
    }

    public Identifier front() {
        return front;
    }

    public Identifier back() {
        return back;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        CubeModel that = (CubeModel) obj;
        return Objects.equals(this.top, that.top) &&
                Objects.equals(this.bottom, that.bottom) &&
                Objects.equals(this.left, that.left) &&
                Objects.equals(this.right, that.right) &&
                Objects.equals(this.front, that.front) &&
                Objects.equals(this.back, that.back);
    }

    @Override
    public int hashCode() {
        return Objects.hash(top, bottom, left, right, front, back);
    }

    @Override
    public String toString() {
        return "CubeModel[" +
                "top=" + top + ", " +
                "bottom=" + bottom + ", " +
                "left=" + left + ", " +
                "right=" + right + ", " +
                "front=" + front + ", " +
                "back=" + back + ']';
    }

    public Set<Identifier> all() {
        return new ReferenceArraySet<>(new Object[]{top, bottom, left, right, front, back});
    }
}

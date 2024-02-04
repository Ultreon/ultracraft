package com.ultreon.craft.client.model.block;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.atlas.TextureAtlas;
import com.ultreon.libs.commons.v0.Identifier;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;

import java.util.Objects;
import java.util.Set;

import static com.ultreon.craft.client.UltracraftClient.isOnMainThread;

public final class CubeModel {
    private final Identifier top;
    private final Identifier bottom;
    private final Identifier left;
    private final Identifier right;
    private final Identifier front;
    private final Identifier back;
    private final ModelProperties properties;

    private CubeModel(Identifier top, Identifier bottom,
                      Identifier left, Identifier right,
                      Identifier front, Identifier back, ModelProperties properties) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.front = front;
        this.back = back;
        this.properties = properties;
    }

    public static CubeModel of(Identifier all) {
        return CubeModel.of(all, all, all);
    }

    public static CubeModel of(Identifier top, Identifier bottom, Identifier side) {
        return CubeModel.of(top, bottom, side, side, side, side);
    }

    public static CubeModel of(Identifier top, Identifier bottom, Identifier side, Identifier front) {
        return CubeModel.of(top, bottom, side, side, front, side);
    }

    public static CubeModel of(Identifier top, Identifier bottom, Identifier side, Identifier front, Identifier back) {
        return CubeModel.of(top, bottom, side, side, front, back);
    }

    public static CubeModel of(Identifier top, Identifier bottom, Identifier left, Identifier right, Identifier front, Identifier back) {
        return new CubeModel(top, bottom, left, right, front, back, ModelProperties.builder().build());
    }

    public static CubeModel of(Identifier all, ModelProperties properties) {
        return CubeModel.of(all, all, all, properties);
    }

    public static CubeModel of(Identifier top, Identifier bottom, Identifier side,  ModelProperties properties) {
        return CubeModel.of(top, bottom, side, side, side, side, properties);
    }

    public static CubeModel of(Identifier top, Identifier bottom, Identifier side, Identifier front, ModelProperties properties) {
        return CubeModel.of(top, bottom, side, side, front, side, properties);
    }

    public static CubeModel of(Identifier top, Identifier bottom, Identifier side, Identifier front, Identifier back, ModelProperties properties) {
        return CubeModel.of(top, bottom, side, side, front, back, properties);
    }

    public static CubeModel of(Identifier top, Identifier bottom, Identifier left, Identifier right, Identifier front, Identifier back, ModelProperties properties) {
        return new CubeModel(top, bottom, left, right, front, back, properties);
    }

    public BakedCubeModel bake(TextureAtlas texture) {
        if (!isOnMainThread()) return UltracraftClient.invokeAndWait(() -> this.bake(texture));
        TextureRegion topTex = texture.get(this.top);
        TextureRegion bottomTex = texture.get(this.bottom);
        TextureRegion leftTex = texture.get(this.left);
        TextureRegion rightTex = texture.get(this.right);
        TextureRegion frontTex = texture.get(this.front);
        TextureRegion backTex = texture.get(this.back);
        BakedCubeModel baked = new BakedCubeModel(
                topTex, bottomTex,
                leftTex, rightTex,
                frontTex, backTex,
                this.properties
        );

        UltracraftClient.get().deferDispose(baked);
        return baked;
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

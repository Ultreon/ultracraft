package com.ultreon.craft.client.model.block;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.atlas.TextureAtlas;
import com.ultreon.craft.crash.ApplicationCrash;
import com.ultreon.craft.crash.CrashCategory;
import com.ultreon.craft.crash.CrashLog;
import com.ultreon.craft.util.ElementID;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

import static com.ultreon.craft.client.UltracraftClient.isOnMainThread;

public final class CubeModel {
    private final ElementID top;
    private final ElementID bottom;
    private final ElementID left;
    private final ElementID right;
    private final ElementID front;
    private final ElementID back;
    private final ModelProperties properties;

    private CubeModel(ElementID top, ElementID bottom,
                      ElementID left, ElementID right,
                      ElementID front, ElementID back, ModelProperties properties) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.front = front;
        this.back = back;
        this.properties = properties;
    }

    public static CubeModel of(ElementID all) {
        return CubeModel.of(all, all, all);
    }

    public static CubeModel of(ElementID top, ElementID bottom, ElementID side) {
        return CubeModel.of(top, bottom, side, side, side, side);
    }

    public static CubeModel of(ElementID top, ElementID bottom, ElementID side, ElementID front) {
        return CubeModel.of(top, bottom, side, side, front, side);
    }

    public static CubeModel of(ElementID top, ElementID bottom, ElementID side, ElementID front, ElementID back) {
        return CubeModel.of(top, bottom, side, side, front, back);
    }

    public static CubeModel of(ElementID top, ElementID bottom, ElementID left, ElementID right, ElementID front, ElementID back) {
        return new CubeModel(top, bottom, left, right, front, back, ModelProperties.builder().build());
    }

    public static CubeModel of(ElementID all, ModelProperties properties) {
        return CubeModel.of(all, all, all, properties);
    }

    public static CubeModel of(ElementID top, ElementID bottom, ElementID side, ModelProperties properties) {
        return CubeModel.of(top, bottom, side, side, side, side, properties);
    }

    public static CubeModel of(ElementID top, ElementID bottom, ElementID side, ElementID front, ModelProperties properties) {
        return CubeModel.of(top, bottom, side, side, front, side, properties);
    }

    public static CubeModel of(ElementID top, ElementID bottom, ElementID side, ElementID front, ElementID back, ModelProperties properties) {
        return CubeModel.of(top, bottom, side, side, front, back, properties);
    }

    public static CubeModel of(ElementID top, ElementID bottom, ElementID left, ElementID right, ElementID front, ElementID back, ModelProperties properties) {
        return new CubeModel(top, bottom, left, right, front, back, properties);
    }

    public BakedCubeModel bake(ElementID resourceId, TextureAtlas texture) {
        if (!isOnMainThread()) return UltracraftClient.invokeAndWait(() -> this.bake(resourceId, texture));
        try {
            TextureRegion topTex = texture.get(this.top);
            TextureRegion bottomTex = texture.get(this.bottom);
            TextureRegion leftTex = texture.get(this.left);
            TextureRegion rightTex = texture.get(this.right);
            TextureRegion frontTex = texture.get(this.front);
            TextureRegion backTex = texture.get(this.back);
            BakedCubeModel baked = new BakedCubeModel(
                    resourceId,
                    topTex, bottomTex,
                    leftTex, rightTex,
                    frontTex, backTex,
                    this.properties
            );

            UltracraftClient.get().deferDispose(baked);
            return baked;
        } catch (RuntimeException e) {
            CrashLog crashLog = createCrash(resourceId, e);

            throw new ApplicationCrash(crashLog);
        }
    }

    @NotNull
    private CrashLog createCrash(ElementID resourceId, RuntimeException e) {
        CrashLog crashLog = new CrashLog("Failed to bake cube model", e);
        CrashCategory bakingModel = new CrashCategory("Baking Model");
        bakingModel.add("ID", resourceId);
        crashLog.addCategory(bakingModel);

        CrashCategory model = new CrashCategory("Model");
        model.add("Top", this.top);
        model.add("Bottom", this.bottom);
        model.add("Left", this.left);
        model.add("Right", this.right);
        model.add("Front", this.front);
        model.add("Back", this.back);
        crashLog.addCategory(model);
        return crashLog;
    }

    public ElementID top() {
        return top;
    }

    public ElementID bottom() {
        return bottom;
    }

    public ElementID left() {
        return left;
    }

    public ElementID right() {
        return right;
    }

    public ElementID front() {
        return front;
    }

    public ElementID back() {
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

    public Set<ElementID> all() {
        return new ReferenceArraySet<>(new Object[]{top, bottom, left, right, front, back});
    }
}

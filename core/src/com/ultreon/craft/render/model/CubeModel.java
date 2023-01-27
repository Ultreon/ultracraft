package com.ultreon.craft.render.model;

import com.badlogic.gdx.graphics.Texture;
import com.ultreon.craft.render.UV;

public record CubeModel(UV top, UV bottom,
                        UV left, UV right,
                        UV front, UV back) {

    public CubeModel(UV top, UV bottom, UV side) {
        this(top, bottom, side, side, side, side);
    }

    public CubeModel(UV all) {
        this(all, all, all, all, all, all);
    }

    public CubeModel(UV top, UV bottom, UV front, UV side) {
        this(top, bottom, side, side, front, side);
    }

    public CubeModel(UV top, UV bottom, UV front, UV back, UV side) {
        this(top, bottom, side, side, front, back);
    }

    public BakedCubeModel bake(Texture texture) {
        return new BakedCubeModel(
                top.bake(texture), bottom.bake(texture),
                left.bake(texture), right.bake(texture),
                front.bake(texture), back.bake(texture)
        );
    }
}

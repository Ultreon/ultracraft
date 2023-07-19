package com.ultreon.craft.render.entity;

import com.badlogic.gdx.math.Quaternion;
import com.ultreon.craft.render.UV;
import com.ultreon.craft.util.BoundingBox;

public class Box {
    private final EntityModelContext context;
    final BoundingBox box;
    Quaternion rotation;
    UV uv;

    public Box(EntityModelContext context, BoundingBox boundingBox) {
        this.context = context;
        this.box = boundingBox;
    }

    public void uv(int u, int v, int uWidth, int vHeight) {
        this.uv = new UV(u, v, uWidth, vHeight, this.context.textureSize().width(), this.context.textureSize().height());
    }

    public void rot(Quaternion quaternion) {
        this.rotation = quaternion;
    }

    public EntityModelContext build() {
        return this.context;
    }
}

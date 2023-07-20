package com.ultreon.craft.render.entity;

import com.badlogic.gdx.math.Quaternion;
import com.google.common.base.Preconditions;
import com.ultreon.craft.render.UV;
import com.ultreon.craft.util.BoundingBox;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.UnknownNullability;

public class Box {
    private final EntityModelContext context;
    final Vec3d position;
    final Vec3d size;
    Quaternion rotation = new Quaternion();
    @UnknownNullability
    UV uv;

    public Box(EntityModelContext context, Vec3d position, Vec3d size) {
        this.context = context;
        this.position = position;
        this.size = size;
    }

    public Box uv(int u, int v, int uWidth, int vHeight) {
        this.uv = new UV(u, v, uWidth, vHeight, this.context.textureSize().width(), this.context.textureSize().height());
        return this;
    }

    public Box rot(Quaternion quaternion) {
        this.rotation = quaternion;
        return this;
    }

    public EntityModelContext build() {
        Preconditions.checkNotNull(this.uv, "UV needs to be set");
        return this.context;
    }
}

package com.ultreon.craft.entity.util;

import java.util.Objects;

public final class EntitySize {
    private final float width;
    private final float height;

    public EntitySize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        EntitySize that = (EntitySize) obj;
        return Float.floatToIntBits(this.width) == Float.floatToIntBits(that.width) &&
                Float.floatToIntBits(this.height) == Float.floatToIntBits(that.height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

    @Override
    public String toString() {
        return "EntitySize{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}

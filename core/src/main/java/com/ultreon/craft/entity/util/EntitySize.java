package com.ultreon.craft.entity.util;

public record EntitySize(float width, float height) {
    @Override
    public String toString() {
        return "EntitySize{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}

package com.ultreon.craft.client.input;

import com.ultreon.libs.commons.v0.vector.Vec2i;

public record TouchPoint(int mouseX, int mouseY, int pointer, int button) {
    public Vec2i pos() {
        return new Vec2i(mouseX, mouseY);
    }
}

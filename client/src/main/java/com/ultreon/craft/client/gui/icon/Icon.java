package com.ultreon.craft.client.gui.icon;

import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.libs.commons.v0.Identifier;

public interface Icon {
    Identifier id();

    int width();

    int height();

    int u();

    int v();

    int texWidth();

    int texHeight();

    default void render(Renderer renderer, int x, int y, float deltaTime) {
        renderer.blit(this.id(), x, y, this.width(), this.height(), this.u(), this.v(), this.width(), this.height(), this.texWidth(), this.texHeight());
    }

    default void render(Renderer renderer, float x, float y, int width, int height, float deltaTime) {
        renderer.blit(this.id(), x, y, width, height, this.u(), this.v(), this.width(), this.height(), this.texWidth(), this.texHeight());
    }
}

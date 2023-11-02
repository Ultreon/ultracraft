package com.ultreon.craft.client.util;

import com.ultreon.craft.client.gui.Renderer;

public interface Renderable {
    void render(Renderer renderer, int mouseX, int mouseY, float deltaTime);
}

package com.ultreon.craft.client.util;

import com.ultreon.craft.client.gui.Renderer;

public interface GameRenderable {
    void render(Renderer renderer, float deltaTime);
}

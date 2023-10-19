package com.ultreon.craft.render;

public interface Renderable {
    void render(Renderer renderer, int mouseX, int mouseY, float deltaTime);
}

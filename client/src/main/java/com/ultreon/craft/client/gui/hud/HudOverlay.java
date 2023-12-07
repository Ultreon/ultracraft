package com.ultreon.craft.client.gui.hud;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.font.Font;
import com.ultreon.craft.client.gui.Renderer;

public abstract class HudOverlay {
    protected final Font font = UltracraftClient.get().font;

    protected abstract void render(Renderer renderer, float deltaTime);
}

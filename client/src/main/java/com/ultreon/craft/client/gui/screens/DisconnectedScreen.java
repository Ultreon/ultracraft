package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.util.Color;

public class DisconnectedScreen extends Screen {
    private final String message;

    public DisconnectedScreen(String message) {
        super("Disconnected");
        this.message = message;
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        super.render(renderer, mouseX, mouseY, deltaTime);
        renderer.drawCenteredTextScaled(this.title, 2, this.width / 2, this.height / 3, Color.WHITE);

        int lineY = 0;
        for (String line : this.message.lines().toList()) {
            renderer.drawCenteredText(line, this.width / 2, this.height / 3 + 30 + lineY * (this.font.lineHeight + 1) - 1, Color.WHITE, false);
            lineY++;
        }

    }
}

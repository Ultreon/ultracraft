package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.util.Color;

public class MessageScreen extends Screen {

    public MessageScreen(String message) {
        super(message);
    }

    @Override
    protected void renderBackground(Renderer renderer) {
        fill(renderer, 0, 0, this.width, this.height, 0xff202020);

        renderer.setColor(Color.rgb(0xffffff));
        renderer.drawTextCenter(this.title, this.width / 2, this.height / 3);
    }

    @Override
    public boolean canClose() {
        return false;
    }
}

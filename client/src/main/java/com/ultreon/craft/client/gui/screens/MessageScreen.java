package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.util.Color;

public class MessageScreen extends Screen {

    public MessageScreen(String message) {
        super(message);
    }

    @Override
    public void build(GuiBuilder builder) {
//        builder.button(() -> new Position(this.size.width / 2, this.size.height / 2))
//                .text("Cancel");
    }

    @Override
    public void renderBackground(Renderer renderer) {
        super.renderBackground(renderer);

        renderer.fill(0, 0, this.size.width, this.size.height, Color.rgb(0x202020));

        renderer.setColor(Color.rgb(0xffffff));
        renderer.drawTextCenter(this.title, this.size.width / 2, this.size.height / 3);
    }

    @Override
    public boolean canClose() {
        return false;
    }
}

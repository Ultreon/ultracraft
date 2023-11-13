package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.text.TextObject;

public class MessageScreen extends Screen {
    private Label messageLabel;

    public MessageScreen(TextObject title) {
        super(title);
    }

    @Override
    public void build(GuiBuilder builder) {
        Label titleLabel = builder.addWithPos(new Label(Alignment.CENTER), () -> new Position(this.size.width / 2, this.size.height / 3 - 40));
        titleLabel.text().set(this.title);
        titleLabel.scale().set(2);

        this.messageLabel = builder.addWithPos(new Label(Alignment.CENTER), () -> new Position(this.size.width / 2, this.size.height / 3));
    }

    @Override
    public void renderBackground(Renderer renderer) {
        super.renderSolidBackground(renderer);
    }

    @Override
    public boolean canClose() {
        return false;
    }
}

package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.text.TranslationText;

public class MessageScreen extends Screen {
    private String deferredMessage = "";
    private Label messageLabel;

    public MessageScreen(TextObject title) {
        super(title);
    }

    public MessageScreen(TranslationText title, String message) {
        super(title);

        this.deferredMessage = message;
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, this.size.height / 3 - 40))
                .scale(2));

        this.messageLabel = builder.add(Label.of(deferredMessage)
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, this.size.height / 3)));
    }

    @Override
    public void renderBackground(Renderer renderer) {
        super.renderSolidBackground(renderer);
    }

    @Override
    public boolean canClose() {
        return false;
    }

    public MessageScreen message(TextObject message) {
        this.messageLabel.text().set(message);
        return this;
    }

    public MessageScreen message(String message) {
        this.messageLabel.text().setRaw(message);
        return this;
    }
}

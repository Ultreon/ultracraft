package com.ultreon.craft.client;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;

public class OutOfMemoryScreen extends Screen {
    protected OutOfMemoryScreen() {
        super(TextObject.translation("ultracraft.screen.out_of_memory"));
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(Label.of(this.getTitle()))
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.getWidth() / 4 , this.getHeight() / 4 - 25))
                .scale(2)
                .textColor(Color.RED.brighter());

        builder.add(Label.of(TextObject.translation("ultracraft.screen.out_of_memory.message")))
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.getWidth() / 4 , this.getHeight() / 4))
                .scale(1);
    }
}

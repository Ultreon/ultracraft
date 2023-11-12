package com.ultreon.craft.client.gui.screens.options;

import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.text.TextObject;

public class OptionsScreen extends Screen {
    public OptionsScreen(String title) {
        super(title);
    }

    public OptionsScreen(TextObject title) {
        super(title);
    }

    public OptionsScreen(String title, Screen parent) {
        super(title, parent);
    }

    public OptionsScreen(TextObject title, Screen parent) {
        super(title, parent);
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.slider(() -> new Position(this.getWidth() / 2 - 200, this.getHeight() / 2 - 25))
                .width(195)
                .callback(it -> this.client.config.get().fov = it.value);
        builder.
    }
}

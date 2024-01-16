package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.client.text.UITranslations;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;

public class ModImportFailedScreen extends Screen {
    public ModImportFailedScreen() {
        super(TextObject.translation("ultracraft.screen.import_failed.title"));
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(Label.of(this.title).alignment(Alignment.CENTER).textColor(Color.RED).position(() -> new Position(this.getWidth() / 2, this.getHeight() / 2 - 30))
                .scale(2));

        builder.add(Label.of(TextObject.translation("ultracraft.screen.import_failed.message"))
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.getWidth() / 2, this.getHeight() / 2)));

        builder.add(TextButton.of(UITranslations.OK, 100)
                .position(() -> new Position(this.getWidth() / 2 - 50, this.getHeight() / 2 + 50))
                .callback(this::onBack));
    }

    private void onBack(TextButton caller) {
        this.back();
    }
}

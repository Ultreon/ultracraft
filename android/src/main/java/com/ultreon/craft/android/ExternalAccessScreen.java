package com.ultreon.craft.android;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.client.text.UITranslations;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;

public class ExternalAccessScreen extends Screen {
    private Runnable onProceed;

    public ExternalAccessScreen(Runnable onProceed) {
        super(TextObject.translation("ultracraft.screen.external_access"));

        this.onProceed = onProceed;
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(Label.of(this.title).alignment(Alignment.CENTER).textColor(Color.RED).position(() -> new Position(this.getWidth() / 2, this.getHeight() / 2 - 30))
                .scale(2));

        builder.add(Label.of(TextObject.translation("ultracraft.screen.restart_confirm.message"))
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.getWidth() / 2, this.getHeight() / 2)));

        builder.add(TextButton.of(UITranslations.PROCEED, 95)
                .position(() -> new Position(this.getWidth() / 2 - 100, this.getHeight() / 2 + 50))
                .callback(this::proceed));

        builder.add(TextButton.of(UITranslations.CANCEL, 95)
                .position(() -> new Position(this.getWidth() / 2 + 5, this.getHeight() / 2 + 50))
                .callback(this::cancel));
    }

    private void cancel(TextButton textButton) {
        this.back();
    }

    private void proceed(TextButton textButton) {
        this.onProceed.run();
    }
}

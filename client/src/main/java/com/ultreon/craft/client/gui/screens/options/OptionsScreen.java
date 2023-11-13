package com.ultreon.craft.client.gui.screens.options;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.screens.LanguageScreen;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.Slider;
import com.ultreon.craft.text.TextObject;

public class OptionsScreen extends Screen {
    private static final TextObject TITLE = TextObject.literal("ultracraft.screen.options.title");
    private Label titleLabel;
    private Slider fovSlider;
    private Button languageButton;
    private Button okButton;

    public OptionsScreen() {
        super(OptionsScreen.TITLE);
    }

    public OptionsScreen(TextObject title, Screen parent) {
        super(OptionsScreen.TITLE, parent);
    }

    @Override
    public void build(GuiBuilder builder) {
        this.titleLabel = builder.addWithPos(new Label(Alignment.CENTER), () -> new Position(this.size.width / 2, this.size.height / 2 - 50));

        this.fovSlider = builder.addWithPos(new Slider(this.client.config.get().fov, 30, 160), () -> new Position(this.getWidth() / 2 - 200, this.getHeight() / 2 - 25));
        this.fovSlider.callback().set(caller -> {
            int fov = caller.value().get();
            this.client.config.get().fov = fov;
            this.client.camera.fieldOfView = fov;
        });
        this.fovSlider.text().set(TextObject.translation("ultracraft.screen.options.fov"));

        this.languageButton = builder.addWithPos(new Button(), () -> new Position(this.size.width / 2 + 5, this.size.height / 2 - 25));
        this.languageButton.callback().set(caller -> this.client.showScreen(new LanguageScreen()));
        this.languageButton.text().set(TextObject.translation("ultracraft.screen.options.language"));

        this.okButton = builder.addWithPos(new Button(), () -> new Position(this.size.width / 2 - 100, this.size.height / 2 + 25));
        this.okButton.callback().set(caller -> {
            this.client.config.save();
            this.back();
        });
        this.okButton.text().translate("ultracraft.ui.ok");
    }

    public Label getTitleLabel() {
        return this.titleLabel;
    }

    public Slider getFovSlider() {
        return this.fovSlider;
    }

    public Button getLanguageButton() {
        return this.languageButton;
    }

    public Button getOkButton() {
        return this.okButton;
    }
}

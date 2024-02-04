package com.ultreon.craft.client.gui.screens.options;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.screens.LanguageScreen;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.Slider;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.client.text.UITranslations;
import com.ultreon.craft.text.TextObject;

public class OptionsScreen extends Screen {
    private static final TextObject TITLE = TextObject.literal("ultracraft.screen.options.title");
    private Label titleLabel;
    private Slider fovSlider;
    private Slider scaleSlider;
    private TextButton languageButton;
    private TextButton okButton;

    public OptionsScreen() {
        super(OptionsScreen.TITLE);
    }

    public OptionsScreen(TextObject title, Screen parent) {
        super(OptionsScreen.TITLE, parent);
    }

    @Override
    public void build(GuiBuilder builder) {
        this.titleLabel = builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .position(() -> new Position(this.size.width / 2, this.size.height / 2 - 50)));

        this.fovSlider = builder.add(Slider.of(200, 30, 160)
                .text(TextObject.translation("ultracraft.screen.options.fov"))
                .value(this.client.config.get().fov)
                .position(() -> new Position(this.getWidth() / 2 - 200, this.getHeight() / 2 - 25))
                .callback(caller -> {
                    int fov = caller.value().get();
                    this.client.config.get().fov = fov;
                    this.client.camera.fieldOfView = fov;
                }));

        this.fovSlider = builder.add(Slider.of(200, 30, 160)
                .text(TextObject.translation("ultracraft.screen.options.guiScale"))
                .value(this.client.config.get().guiScale)
                .position(() -> new Position(this.getWidth() / 2 - 200, this.getHeight() / 2 - 25))
                .callback(caller -> {
                    if (caller.value().get() == 0) {
                        this.client.setAutomaticScale(true);
                    } else {
                        this.client.setAutomaticScale(false);
                        this.client.setGuiScale(caller.value().get());
                    }
                }));

        this.languageButton = builder.add(TextButton.of(TextObject.translation("ultracraft.screen.options.language"))
                .position(() -> new Position(this.size.width / 2 + 5, this.size.height / 2 - 25))
                .callback(caller -> this.client.showScreen(new LanguageScreen())));

        this.okButton = builder.add(TextButton.of(UITranslations.OK)
                .position(() -> new Position(this.size.width / 2 - 100, this.size.height / 2 + 25))
                .callback(caller -> {
                    this.client.config.save();
                    this.back();
                }));
    }

    public Label getTitleLabel() {
        return this.titleLabel;
    }

    public Slider getFovSlider() {
        return this.fovSlider;
    }

    public TextButton getLanguageButton() {
        return this.languageButton;
    }

    public TextButton getOkButton() {
        return this.okButton;
    }
}

package com.ultreon.craft.client.gui.screens.options;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.screens.LanguageScreen;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.widget.CycleButton;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.Slider;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.client.text.UITranslations;
import com.ultreon.craft.text.TextObject;

public class OptionsScreen extends Screen {
    private static final TextObject TITLE = TextObject.translation("ultracraft.screen.options.title");
    private Label titleLabel;
    private Slider fovSlider;
    private Slider renderDistanceSlider;
    private CycleButton<Scale> guiScaleSlider;
    private CycleButton<BooleanEnum> fullscreenButton;
    private TextButton languageButton;
    private TextButton privacyButton;
    private TextButton videoSettingsButton;
    private TextButton accessibilityButton;
    private TextButton personalSettingsButton;
    private TextButton okButton;

    public OptionsScreen() {
        super(OptionsScreen.TITLE);
    }

    public OptionsScreen(Screen parent) {
        super(OptionsScreen.TITLE, parent);
    }

    @Override
    public void build(GuiBuilder builder) {
        this.titleLabel = builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(this.size.width / 2, this.size.height / 2 - 85)));

        this.languageButton = builder.add(TextButton.of(TextObject.translation("ultracraft.screen.options.language"))
                .bounds(() -> new Bounds(this.size.width / 2 - 152, this.size.height / 2 - 50, 150, 21))
                .callback(caller -> this.client.showScreen(new LanguageScreen())));

        this.privacyButton = builder.add(TextButton.of(TextObject.translation("ultracraft.screen.options.privacy"))
                .bounds(() -> new Bounds(this.size.width / 2 + 2, this.size.height / 2 - 50, 150, 21))
                .callback(caller -> this.client.showScreen(new PrivacySettingsScreen())));

        this.videoSettingsButton = builder.add(TextButton.of(TextObject.translation("ultracraft.screen.options.video"))
                .bounds(() -> new Bounds(this.size.width / 2 - 152, this.size.height / 2 - 25, 150, 21))
                .callback(caller -> this.client.showScreen(new VideoSettingsScreen())));

        this.accessibilityButton = builder.add(TextButton.of(TextObject.translation("ultracraft.screen.options.accessibility"))
                .bounds(() -> new Bounds(this.size.width / 2 + 2, this.size.height / 2 - 25, 150, 21))
                .callback(caller -> this.client.showScreen(new AccessibilitySettingsScreen())));

        this.personalSettingsButton = builder.add(TextButton.of(TextObject.translation("ultracraft.screen.options.personalisation"))
                .bounds(() -> new Bounds(this.size.width / 2 + -152, this.size.height / 2, 304, 21))
                .callback(caller -> this.client.showScreen(new PersonalSettingsScreen())));

        this.okButton = builder.add(TextButton.of(UITranslations.OK)
                .bounds(() -> new Bounds(this.size.width / 2 - 75, this.size.height / 2 + 50, 150, 21))
                .callback(caller -> this.back()));
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

    public CycleButton<Scale> getGuiScaleButton() {
        return guiScaleSlider;
    }

    public CycleButton<BooleanEnum> getFullscreenButton() {
        return fullscreenButton;
    }

    public Slider getRenderDistanceSlider() {
        return renderDistanceSlider;
    }

    public enum Scale {
        AUTO(0),
        SMALL(1),
        MEDIUM(2),
        LARGE(3);

        private final int value;

        Scale(int value) {
            this.value = value;
        }

        public int get() {
            return this.value;
        }

        public static Scale of(int value) {
            for (Scale scale : Scale.values()) {
                if (scale.value == value) {
                    return scale;
                }
            }
            return null;
        }
    }

    public enum BooleanEnum {
        TRUE(true),
        FALSE(false);

        private final boolean value;

        BooleanEnum(boolean value) {
            this.value = value;
        }

        public boolean get() {
            return this.value;
        }

        public static BooleanEnum of(boolean value) {
            for (BooleanEnum booleanEnum : BooleanEnum.values()) {
                if (booleanEnum.value == value) {
                    return booleanEnum;
                }
            }
            throw new InternalError("Invalid boolean value: " + value);
        }
    }
}

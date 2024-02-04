package com.ultreon.craft.client.gui.screens.options;

import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.screens.options.OptionsScreen.BooleanEnum;
import com.ultreon.craft.client.gui.widget.CycleButton;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.client.text.UITranslations;
import com.ultreon.craft.text.TextObject;

public class PersonalSettingsScreen extends Screen {
    private static final TextObject TITLE = TextObject.translation("ultracraft.screen.options.personalisation.title");

    public PersonalSettingsScreen() {
        super(PersonalSettingsScreen.TITLE);
    }

    public PersonalSettingsScreen(Screen parent) {
        super(PersonalSettingsScreen.TITLE, parent);
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(this.size.width / 2, this.size.height / 2 - 85)));

        builder.add(new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().personalisation.diagonalFontShadow ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .label(TextObject.translation("ultracraft.screen.options.personalisation.diagonalFontShadow"))
                .bounds(() -> new Bounds(this.getWidth() / 2 - 152, this.getHeight() / 2 - 50, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "ultracraft.ui.enabled" : "ultracraft.ui.disabled"))
                .callback(this::setDiagonalFontShadow)
        );

        builder.add(new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().personalisation.enforceUnicode ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .label(TextObject.translation("ultracraft.screen.options.personalisation.enforceUnicode"))
                .bounds(() -> new Bounds(this.getWidth() / 2 + 2, this.getHeight() / 2 - 50, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "ultracraft.ui.enabled" : "ultracraft.ui.disabled"))
                .callback(this::setEnforceUnicode)
        );

        builder.add(TextButton.of(UITranslations.OK)
                .bounds(() -> new Bounds(this.size.width / 2 - 75, this.size.height / 2, 150, 21))
                .callback(caller -> this.back()));
    }

    private void setEnforceUnicode(CycleButton<BooleanEnum> booleanEnumCycleButton) {
        this.client.config.get().personalisation.enforceUnicode = booleanEnumCycleButton.getValue().get();
        this.client.config.save();
    }

    private void setDiagonalFontShadow(CycleButton<BooleanEnum> booleanEnumCycleButton) {
        this.client.config.get().personalisation.diagonalFontShadow = booleanEnumCycleButton.getValue().get();
        this.client.config.save();
    }
}

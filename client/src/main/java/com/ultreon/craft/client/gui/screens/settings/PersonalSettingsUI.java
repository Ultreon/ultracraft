package com.ultreon.craft.client.gui.screens.settings;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.screens.options.OptionsScreen.BooleanEnum;
import com.ultreon.craft.client.gui.screens.tabs.TabBuilder;
import com.ultreon.craft.client.gui.widget.CycleButton;
import com.ultreon.craft.client.gui.widget.Label;
import com.ultreon.craft.client.gui.widget.Slider;
import com.ultreon.craft.text.TextObject;

public class PersonalSettingsUI {
    static final TextObject TITLE = TextObject.translation("ultracraft.screen.options.personalisation.title");
    private UltracraftClient client;

    public void build(TabBuilder builder) {
        this.client = builder.client();
        
        builder.add(Label.of(builder.title())
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(builder.content().getX() + 235, builder.content().getY() + 25)));

        builder.add(TextObject.translation("ultracraft.screen.options.personalisation.diagonalFontShadow"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().personalisation.diagonalFontShadow ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 50, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "ultracraft.ui.enabled" : "ultracraft.ui.disabled"))
                .callback(this::setDiagonalFontShadow));

        builder.add(TextObject.translation("ultracraft.screen.options.personalisation.enforceUnicode"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().personalisation.enforceUnicode ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 75, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "ultracraft.ui.enabled" : "ultracraft.ui.disabled"))
                .callback(this::setEnforceUnicode));

        builder.add(TextObject.translation("ultracraft.screen.options.personalisation.blurRadius"), new Slider(this.client.config.get().personalisation.blurRadius == 0 ? 32 : (int) this.client.config.get().personalisation.blurRadius.floatValue(), 4, 128)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 150, 150, 21))
                .text(TextObject.translation("ultracraft.screen.options.personalisation.blurRadius.text"))
                .callback(this::setBlurRadius));
    }

    private void setBlurRadius(Slider integerCycleButton) {
        this.client.config.get().personalisation.blurRadius = (double) integerCycleButton.value().get();
        this.client.config.save();
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

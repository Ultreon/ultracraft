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

public class AccessibilitySettingsScreen extends Screen {
    private static final TextObject TITLE = TextObject.translation("ultracraft.screen.options.accessibility.title");

    protected AccessibilitySettingsScreen() {
        super(TITLE);
    }

    public void build(GuiBuilder builder) {
        builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(builder.screen().getWidth() / 2, builder.screen().getHeight() / 2 - 85)));

        builder.add(new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().accessibility.hideFirstPersonPlayer ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .label(TextObject.translation("ultracraft.screen.options.accessibility.hideFirstPersonPlayer"))
                .bounds(() -> new Bounds(builder.screen().getWidth() / 2 - 152, builder.screen().getHeight() / 2 - 50, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("ultracraft.ui.hidden") : TextObject.translation("ultracraft.ui.visible"))
                .callback(this::setHideFirstPersonPlayer));

        builder.add(new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().accessibility.hideHotbarWhenThirdPerson ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .label(TextObject.translation("ultracraft.screen.options.accessibility.hideHotbarWhenThirdPerson"))
                .bounds(() -> new Bounds(builder.screen().getWidth() / 2 + 2, builder.screen().getHeight() / 2 - 50, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "ultracraft.ui.hidden" : "ultracraft.ui.visible"))
                .callback(this::setHideHotbarWhenThirdPerson)
        );

        builder.add(new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().accessibility.vibration ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .label(TextObject.translation("ultracraft.screen.options.accessibility.vibration"))
                .bounds(() -> new Bounds(builder.screen().getWidth() / 2 - 152, builder.screen().getHeight() / 2 - 25, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("ultracraft.ui.enabled") : TextObject.translation("ultracraft.ui.disabled"))
                .callback(this::setVibration));

        builder.add(TextButton.of(UITranslations.OK)
                .bounds(() -> new Bounds(builder.screen().getWidth() / 2 - 75, builder.screen().getHeight() / 2 + 25, 150, 21))
                .callback(caller -> builder.screen().back()));
    }

    private void setHideFirstPersonPlayer(CycleButton<BooleanEnum> value) {
        this.client.config.get().accessibility.hideFirstPersonPlayer = value.getValue() == BooleanEnum.TRUE;
        this.client.config.save();
    }

    private void setHideHotbarWhenThirdPerson(CycleButton<BooleanEnum> value) {
        this.client.config.get().accessibility.hideHotbarWhenThirdPerson = value.getValue() == BooleanEnum.TRUE;
        this.client.config.save();
    }

    private void setVibration(CycleButton<BooleanEnum> value) {
        this.client.config.get().accessibility.vibration = value.getValue() == BooleanEnum.TRUE;
        this.client.config.save();
    }
}

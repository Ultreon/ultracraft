package com.ultreon.craft.client.gui.screens.settings;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.screens.options.OptionsScreen.BooleanEnum;
import com.ultreon.craft.client.gui.screens.tabs.TabBuilder;
import com.ultreon.craft.client.gui.widget.*;
import com.ultreon.craft.client.text.UITranslations;
import com.ultreon.craft.text.TextObject;

public class AccessibilitySettingsUI {
    static final TextObject TITLE = TextObject.translation("ultracraft.screen.options.accessibility.title");
    private UltracraftClient client;

    public AccessibilitySettingsUI() {
        super();
    }

    public void build(TabBuilder builder) {
        this.client = builder.client();
        builder.add(Label.of(builder.title())
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(builder.content().getX() + 235, builder.content().getY() + 25)));

        builder.add(TextObject.translation("ultracraft.screen.options.accessibility.hideFirstPersonPlayer"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().accessibility.hideFirstPersonPlayer ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 50, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("ultracraft.ui.hidden") : TextObject.translation("ultracraft.ui.visible"))
                .callback(this::setHideFirstPersonPlayer));

        builder.add(TextObject.translation("ultracraft.screen.options.accessibility.hideHotbarWhenThirdPerson"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().accessibility.hideHotbarWhenThirdPerson ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 75, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "ultracraft.ui.hidden" : "ultracraft.ui.visible"))
                .callback(this::setHideHotbarWhenThirdPerson)
        );

        builder.add(TextObject.translation("ultracraft.screen.options.accessibility.vibration"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().accessibility.vibration ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 100, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("ultracraft.ui.enabled") : TextObject.translation("ultracraft.ui.disabled"))
                .callback(this::setVibration));
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

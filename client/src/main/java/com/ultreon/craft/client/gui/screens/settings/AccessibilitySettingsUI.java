package com.ultreon.craft.client.gui.screens.settings;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.config.Config;
import com.ultreon.craft.client.gui.Alignment;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.screens.options.BooleanEnum;
import com.ultreon.craft.client.gui.screens.tabs.TabBuilder;
import com.ultreon.craft.client.gui.widget.CycleButton;
import com.ultreon.craft.client.gui.widget.Label;
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
                .value(Config.hideFirstPersonPlayer ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 50, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("ultracraft.ui.hidden") : TextObject.translation("ultracraft.ui.visible"))
                .callback(this::setHideFirstPersonPlayer));

        builder.add(TextObject.translation("ultracraft.screen.options.accessibility.hideHotbarWhenThirdPerson"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(Config.hideHotbarWhenThirdPerson ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 75, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "ultracraft.ui.hidden" : "ultracraft.ui.visible"))
                .callback(this::setHideHotbarWhenThirdPerson)
        );

        builder.add(TextObject.translation("ultracraft.screen.options.accessibility.vibration"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(Config.vibration ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 100, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("ultracraft.ui.enabled") : TextObject.translation("ultracraft.ui.disabled"))
                .callback(this::setVibration));
    }

    private void setHideFirstPersonPlayer(CycleButton<BooleanEnum> value) {
        Config.hideFirstPersonPlayer = value.getValue() == BooleanEnum.TRUE;
        this.client.newConfig.save();
    }

    private void setHideHotbarWhenThirdPerson(CycleButton<BooleanEnum> value) {
        Config.hideHotbarWhenThirdPerson = value.getValue() == BooleanEnum.TRUE;
        this.client.newConfig.save();
    }

    private void setVibration(CycleButton<BooleanEnum> value) {
        Config.vibration = value.getValue() == BooleanEnum.TRUE;
        this.client.newConfig.save();
    }
}

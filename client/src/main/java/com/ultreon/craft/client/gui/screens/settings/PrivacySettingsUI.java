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

public class PrivacySettingsUI {
    static final TextObject TITLE = TextObject.translation("ultracraft.screen.options.privacy.title");
    private UltracraftClient client;

    public void build(TabBuilder builder) {
        this.client = builder.client();

        builder.add(Label.of(builder.title())
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(builder.content().getX() + 235, builder.content().getY() + 25)));
        
        builder.add(TextObject.translation("ultracraft.screen.options.privacy.hideActiveServer"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(Config.hideActiveServerFromRPC ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 50, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("ultracraft.ui.hidden") : TextObject.translation("ultracraft.ui.visible"))
                .callback(this::setHideActiveServer));

        builder.add(TextObject.translation("ultracraft.screen.options.privacy.hideRpc"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(Config.hideRPC ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 75, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "ultracraft.ui.hidden" : "ultracraft.ui.visible"))
                .callback(this::setHideActivity)
        );

        builder.add(TextObject.translation("ultracraft.screen.options.privacy.hidePlayerName"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(Config.hideUsername ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 100, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("ultracraft.ui.hidden") : TextObject.translation("ultracraft.ui.visible"))
                .callback(this::setHidePlayerNames));

        builder.add(TextObject.translation("ultracraft.screen.options.privacy.hidePlayerSkin"), new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(Config.hideSkin ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .bounds(() -> new Bounds(builder.content().getX() + 160, builder.content().getY() + 125, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "ultracraft.ui.hidden" : "ultracraft.ui.visible"))
                .callback(this::setHidePlayerSkin));
    }

    private void setHideActiveServer(CycleButton<BooleanEnum> button) {
        Config.hideActiveServerFromRPC = button.getValue().get();
        this.client.newConfig.save();
    }
    
    private void setHideActivity(CycleButton<BooleanEnum> button) {
        Config.hideRPC = button.getValue().get();
        this.client.newConfig.save();
    }

    private void setHidePlayerNames(CycleButton<BooleanEnum> button) {
        Config.hideUsername = button.getValue().get();
        this.client.newConfig.save();
    }

    private void setHidePlayerSkin(CycleButton<BooleanEnum> button) {
        Config.hideSkin = button.getValue().get();
        this.client.newConfig.save();
    }
}

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

public class PrivacySettingsScreen extends Screen {
    private static final TextObject TITLE = TextObject.translation("ultracraft.screen.options.privacy.title");

    public PrivacySettingsScreen() {
        super(PrivacySettingsScreen.TITLE);
    }

    public PrivacySettingsScreen(Screen parent) {
        super(PrivacySettingsScreen.TITLE, parent);
    }

    @Override
    public void build(GuiBuilder builder) {
        builder.add(Label.of(this.title)
                .alignment(Alignment.CENTER)
                .scale(2)
                .position(() -> new Position(this.size.width / 2, this.size.height / 2 - 85)));
        
        builder.add(new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().privacy.hideActiveServer ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .label(TextObject.translation("ultracraft.screen.options.privacy.hideActiveServer"))
                .bounds(() -> new Bounds(this.getWidth() / 2 - 152, this.getHeight() / 2 - 50, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("ultracraft.ui.hidden") : TextObject.translation("ultracraft.ui.visible"))
                .callback(this::setHideActiveServer));

        builder.add(new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().privacy.hideRpc ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .label(TextObject.translation("ultracraft.screen.options.privacy.hideRpc"))
                .bounds(() -> new Bounds(this.getWidth() / 2 + 2, this.getHeight() / 2 - 50, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "ultracraft.ui.hidden" : "ultracraft.ui.visible"))
                .callback(this::setHideActivity)
        );

        builder.add(new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().privacy.hidePlayerName ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .label(TextObject.translation("ultracraft.screen.options.privacy.hidePlayerName"))
                .bounds(() -> new Bounds(this.getWidth() / 2 - 152, this.getHeight() / 2 - 25, 150, 21))
                .formatter(scale -> scale == BooleanEnum.TRUE ? TextObject.translation("ultracraft.ui.hidden") : TextObject.translation("ultracraft.ui.visible"))
                .callback(this::setHidePlayerNames));

        builder.add(new CycleButton<BooleanEnum>()
                .values(BooleanEnum.values())
                .value(this.client.config.get().privacy.hidePlayerSkin ? BooleanEnum.TRUE : BooleanEnum.FALSE)
                .label(TextObject.translation("ultracraft.screen.options.privacy.hidePlayerSkin"))
                .bounds(() -> new Bounds(this.getWidth() / 2 + 2, this.getHeight() / 2 - 25, 150, 21))
                .formatter(booleanEnum -> TextObject.translation(booleanEnum == BooleanEnum.TRUE ? "ultracraft.ui.hidden" : "ultracraft.ui.visible"))
                .callback(this::setHidePlayerSkin));

        builder.add(TextButton.of(UITranslations.OK)
                .bounds(() -> new Bounds(this.size.width / 2 - 75, this.size.height / 2 + 25, 150, 21))
                .callback(caller -> this.back()));
    }

    private void setHideActiveServer(CycleButton<BooleanEnum> button) {
        this.client.config.get().privacy.hideActiveServer = button.getValue().get();
        this.client.config.save();
    }
    
    private void setHideActivity(CycleButton<BooleanEnum> button) {
        this.client.config.get().privacy.hideRpc = button.getValue().get();
        this.client.config.save();
    }

    private void setHidePlayerNames(CycleButton<BooleanEnum> button) {
        this.client.config.get().privacy.hidePlayerName = button.getValue().get();
        this.client.config.save();
    }

    private void setHidePlayerSkin(CycleButton<BooleanEnum> button) {
        this.client.config.get().privacy.hidePlayerSkin = button.getValue().get();
        this.client.config.save();
    }
}

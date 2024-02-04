package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.widget.TextButton;
import com.ultreon.craft.client.gui.widget.TextEntry;
import com.ultreon.craft.client.text.UITranslations;
import com.ultreon.craft.text.TextObject;
import com.ultreon.libs.translations.v1.Language;
import org.jetbrains.annotations.Nullable;

public class MultiplayerScreen extends Screen {
    private TextEntry entry;
    private TextButton joinButton;

    public MultiplayerScreen() {
        super(Language.translate("ultracraft.screen.multiplayer"));
    }

    public MultiplayerScreen(@Nullable Screen back) {
        super(TextObject.translation("ultracraft.screen.multiplayer"), back);
    }

    @Override
    public void build(GuiBuilder builder) {
        this.entry = builder.add(TextEntry.of().position(() -> new Position(this.size.width / 2 - 100, this.size.height / 2 - 10)))
                .callback(this::validateServerIp)
                .hint(TextObject.translation("ultracraft.screen.multiplayer.server_ip"));

        this.joinButton = builder.add(TextButton.of(TextObject.translation("ultracraft.screen.multiplayer.join"), 98)
                        .position(() -> new Position(this.size.width / 2 + 2, this.size.height / 2 + 15)))
                .callback(this::joinServer);
        builder.add(TextButton.of(UITranslations.BACK, 98)
                        .position(() -> new Position(this.size.width / 2 - 100, this.size.height / 2 + 15)))
                .callback(caller -> this.back());
        this.joinButton.disable();
    }

    private void joinServer(TextButton caller) {
        caller.enabled = false;
        MessageScreen messageScreen = new MessageScreen(TextObject.translation("ultracraft.screen.message.joining_server"));
        this.client.showScreen(messageScreen);

        String[] split = this.entry.getValue().split(":", 2);
        if (split.length < 2) {
            return;
        }

        try {
            this.client.connectToServer(split[0], Integer.parseInt(split[1]));
        } catch (Exception e) {
            UltracraftClient.LOGGER.error("Can't connect to server", e);
        }
    }

    private void validateServerIp(TextEntry caller) {
        var text = caller.getValue();
        boolean matches = text.matches("[^:]+:\\d{1,5}");
        if (!matches) {
            this.joinButton.enabled = false;
            return;
        }
        var split = text.split(":", 2);
        var port = Integer.parseInt(split[1]);

        if (port < 0 || port > 65535) {
            this.joinButton.enabled = false;
            return;
        }

        this.joinButton.enabled = true;
    }
}

package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.TextEntry;
import com.ultreon.craft.text.TextObject;
import com.ultreon.libs.translations.v1.Language;
import org.jetbrains.annotations.Nullable;

public class MultiplayerScreen extends Screen {
    private TextEntry<?> entry;
    private Button<?> joinButton;

    public MultiplayerScreen() {
        super(Language.translate("ultracraft.screen.multiplayer"));
    }

    public MultiplayerScreen(@Nullable Screen back) {
        super(TextObject.translation("ultracraft.screen.multiplayer"), back);
    }

    @Override
    public void build(GuiBuilder builder) {
        this.entry = builder.textEntry(() -> new Position(this.size.width / 2 - 100, this.size.height / 2 - 10), this::validateServerIp)
                .hint(TextObject.translation("ultracraft.screen.multiplayer.server_ip"));

        this.joinButton = builder.button(() -> new Position(this.size.width / 2 - 100, this.size.height / 2 + 15), this::joinServer)
                .translation("ultracraft.screen.multiplayer.join")
                .enabled(false);
    }

    private void joinServer(Button<?> caller) {
        caller.enabled = false;
        MessageScreen messageScreen = new MessageScreen(TextObject.translation("ultracraft.screen.message.joining_server"));
        this.client.showScreen(messageScreen);

        String[] split = this.entry.getRawText().split(":", 2);
        if (split.length < 2) {
            return;
        }

        try {
            this.client.connectToServer(split[0], Integer.parseInt(split[1]));
        } catch (Exception e) {
            UltracraftClient.LOGGER.error("Can't connect to server", e);
        }
    }

    private void validateServerIp(TextEntry<?> caller) {
        var text = caller.getRawText();
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

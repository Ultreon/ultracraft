package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.TextEntry;
import com.ultreon.craft.client.network.ClientConnections;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.libs.translations.v1.Language;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

public class MultiplayerScreen extends Screen {
    private TextEntry entry;
    private Button joinButton;

    public MultiplayerScreen() {
        super(Language.translate("craft.screen.multiplayer"));
    }

    public MultiplayerScreen(@Nullable Screen back) {
        super(back, Language.translate("craft.screen.multiplayer"));
    }

    @Override
    public void init() {
        this.entry = this.add(new TextEntry(this.width / 2 - 100, this.height / 2 - 10, 200, 21, Language.translate("craft.screen.multiplayer.server_ip")));
        this.entry.setCallback(caller -> {
            var text = caller.getText();
            boolean matches = text.matches("[^:]+:\\d{1,5}");
            if (!matches) {
                this.joinButton.enabled = false;
                return;
            }
            var split = text.split(":", 2);
            var hostname = split[0];
            var port = Integer.parseInt(split[1]);

            if (port < 0 || port > 65535) {
                this.joinButton.enabled = false;
                return;
            }

            this.joinButton.enabled = true;
        });
        this.joinButton = this.add(new Button(this.width / 2 - 100, this.height / 2 + 15, 200, Language.translate("craft.screen.multiplayer.join"), caller -> {
            caller.enabled = false;
            MessageScreen messageScreen = new MessageScreen("Joining server");
            this.client.showScreen(messageScreen);

            String[] split = this.entry.getText().split(":", 2);
            if (split.length < 2) {
                return;
            }

            try {
                this.client.connectToServer(split[0], Integer.parseInt(split[1]));
            } catch (Exception e) {
                UltracraftClient.LOGGER.error("Can't connect to server", e);
            }
        }));
    }
}

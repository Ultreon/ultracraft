package com.ultreon.craft.client.gui.screens;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.widget.Button;
import com.ultreon.craft.client.gui.widget.TextEntry;
import com.ultreon.libs.translations.v1.Language;
import org.jetbrains.annotations.Nullable;

public class MultiplayerScreen extends Screen {
    private TextEntry ipEntry;
    private TextEntry pswEntry;
    private Button joinButton;

    public MultiplayerScreen() {
        super(Language.translate("craft.screen.multiplayer"));
    }

    public MultiplayerScreen(@Nullable Screen back) {
        super(back, Language.translate("craft.screen.multiplayer"));
    }

    @Override
    public void init() {
        this.ipEntry = this.add(new TextEntry(this.width / 2 - 100, this.height / 2 - 35, 200, 21, Language.translate("craft.screen.multiplayer.server_ip")));
        this.ipEntry.setCallback(caller -> {
            this.revalidate();
        });

        this.pswEntry = this.add(new TextEntry(this.width / 2 - 100, this.height / 2 - 10, 200, 21, Language.translate("craft.screen.multiplayer.password")));
        this.pswEntry.setPassword(true);
        this.pswEntry.setCallback(caller -> {
            var text = caller.getText();
            this.joinButton.enabled = text.matches("[a-zA-Z0-9.,!?:;\\-+_]{5,16}");
        });

        this.joinButton = this.add(new Button(this.width / 2, this.height / 2 + 15, 100, Language.translate("craft.screen.multiplayer.join"), caller -> {
            caller.enabled = false;
            String[] split = this.ipEntry.getText().split(":", 2);
            if (split.length < 1) {
                return;
            }

            String hostname = split[0];
            int port = split.length == 1 ? 38800 :  Integer.parseInt(split[1]);

            try {
                MessageScreen messageScreen = new MessageScreen("Joining server");
                this.client.showScreen(messageScreen);

                this.client.connectToServer(hostname, port, this.pswEntry.getText());
            } catch (Exception e) {
                UltracraftClient.LOGGER.error("Can't connect to server", e);
            }

            this.pswEntry.setText(""); // Clear the password for security reasons.
        }));

        this.revalidate();
    }

    private void revalidate() {
        var text = this.ipEntry.getText();
        boolean matches = text.matches("[^:]+:\\d{1,5}");
        if (!matches) {
            this.joinButton.enabled = false;
            return;
        }

        var split = text.split(":", 2);
        if (split.length < 1) {
            this.joinButton.enabled = false;
            return;
        }

        var port = split.length == 1 ? 38800 :  Integer.parseInt(split[1]);
        if (port < 0 || port > 65535) {
            this.joinButton.enabled = false;
            return;
        }

        this.joinButton.enabled = true;

        var psw = this.pswEntry.getText();
        this.joinButton.enabled = psw.matches("[a-zA-Z0-9.,!?:;\\-+_]{5,16}");
    }
}

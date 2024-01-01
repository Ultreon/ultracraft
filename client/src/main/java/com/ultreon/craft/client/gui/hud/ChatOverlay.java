package com.ultreon.craft.client.gui.hud;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.font.Font;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.screens.ChatScreen;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import it.unimi.dsi.fastutil.longs.LongList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChatOverlay extends HudOverlay {
    public ChatOverlay() {
        super();
    }

    public static void renderChatOverlay(Font font, @NotNull Renderer renderer, boolean showAnyways) {
        int y = UltracraftClient.get().getScaledHeight() - 40;
        List<TextObject> messages = ChatScreen.getMessages();
        LongList messageTimestamps = ChatScreen.getMessageTimestamps();
        for (int i = 0, messagesSize = messages.size(); i < messagesSize; i++) {
            TextObject text = messages.get(i);
            long messageTimestamp = messageTimestamps.getLong(i);
            long millisAgo = System.currentTimeMillis() - messageTimestamp;
            if (millisAgo <= 4000 || showAnyways) {
                if (millisAgo <= 3000 || showAnyways) {
                    renderer.textLeft(text, 10, y, Color.WHITE);
                } else {
                    int alpha = (int) (255 * (millisAgo - 3000) / 1000) % 1000;
                    renderer.setColor(Color.WHITE.withAlpha(alpha));
                    renderer.setBlitColor(Color.WHITE.withAlpha(alpha));
                    renderer.textLeft(text, 10, y);
                    renderer.setColor(Color.WHITE);
                    renderer.setBlitColor(Color.WHITE);
                }
            }
            y -= font.lineHeight + 2;
        }
    }

    @Override
    public void render(Renderer renderer, float deltaTime) {
        ChatOverlay.renderChatOverlay(this.font, renderer, false);
    }
}

package com.ultreon.craft.client.gui.hud;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.font.Font;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.screens.ChatScreen;
import com.ultreon.craft.crash.CrashLog;
import com.ultreon.craft.text.FormattedText;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import it.unimi.dsi.fastutil.longs.LongList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatOverlay extends HudOverlay {
    private static final Lock messageLock = new ReentrantLock(true);

    public ChatOverlay() {
        super();
    }

    public static void renderChatOverlay(Font font, @NotNull Renderer renderer, boolean showAnyways) {
        int y = UltracraftClient.get().getScaledHeight() - 40;
        List<TextObject> messages = ChatScreen.getMessages();
        LongList messageTimestamps = ChatScreen.getMessageTimestamps();
        messageLock.lock();
        try {
            for (int i = 0, messagesSize = messages.size(); i < messagesSize; i++) {
                TextObject text = messages.get(i);
                if (text == null) continue;
                long lineCount = text.getText().lines().count();
                if (lineCount == 0) continue;
                if (lineCount > 1) y -= (int) ((font.lineHeight + 2) * (lineCount - 1));
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
        } catch (Exception e) {
            messageLock.unlock();
            UltracraftClient.crash(new CrashLog("Error rendering chat overlay", e));
            throw new Error("Unreachable");
        }
        messageLock.unlock();
    }

    @Override
    public void render(Renderer renderer, float deltaTime) {
        ChatOverlay.renderChatOverlay(this.font, renderer, false);
    }
}

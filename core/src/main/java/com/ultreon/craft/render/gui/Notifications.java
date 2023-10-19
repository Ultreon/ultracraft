package com.ultreon.craft.render.gui;

import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Renderable;
import com.ultreon.craft.render.Renderer;
import com.ultreon.libs.datetime.v0.Duration;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Notifications implements Renderable {
    private static final int NOTIFICATION_HEIGHT = 80;
    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_OFFSET = 20;
    private static final int NOTIFICATION_GAP = 10;

    private final Lock lock = new ReentrantLock(true);
    private final UltreonCraft game;
    private final Deque<Notification> notifications = new ArrayDeque<>();
    private final Set<UUID> usedNotifications = new HashSet<>();

    public Notifications(UltreonCraft game) {
        this.game = game;
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        if (this.game.isLoading()) return;

        int x = this.game.getWidth() - NOTIFICATION_OFFSET - NOTIFICATION_WIDTH;
        int y = NOTIFICATION_OFFSET;

        this.lock.lock();
        this.notifications.removeIf(Notification::isDead);
        for (Notification notification : this.notifications) {
            String title = notification.getTitle();
            String summary = notification.getSummary();
            String subText = notification.getSubText();
            float motionRatio = notification.getMotion();
            int motion = (int) ((NOTIFICATION_WIDTH + NOTIFICATION_OFFSET) * motionRatio);

            renderer.fill(x + motion, y, NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT, Color.rgb(0x101010));
            renderer.box(x + motion + 5, y + 5, NOTIFICATION_WIDTH - 10, NOTIFICATION_HEIGHT - 10, Color.rgb(0x505050));

            renderer.drawText(title, x + motion + 10, y + 13, Color.rgb(0xd0d0d0));
            renderer.drawText(summary, x + motion + 10, y + 40, Color.rgb(0xb0b0b0));
            renderer.drawText(subText == null ? "" : subText, x + motion + 10, y + 60, Color.rgb(0x707070));

            y += NOTIFICATION_HEIGHT + NOTIFICATION_GAP;
        }
        this.lock.unlock();
    }

    public void notify(Notification notification) {
        if (!UltreonCraft.isOnRenderingThread()) {
            UltreonCraft.invoke(() -> this.notify(notification));
            return;
        }

        this.notifications.addLast(notification);
    }

    public void notifyOnce(UUID uuid, Notification message) {
        if (!this.usedNotifications.contains(uuid)) {
            this.usedNotifications.add(uuid);
            this.notify(message);
        }
    }

    public void unavailable(String feature) {
        this.notify(Notification.builder("Unavailable Feature", String.format("'%s' isn't available yet.", feature))
                .subText("Feature Locker")
                .duration(Duration.ofSeconds(5))
                .build()
        );
    }
}

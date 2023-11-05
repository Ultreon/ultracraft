package com.ultreon.craft.client.gui;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.util.Color;
import com.ultreon.craft.client.util.Renderable;
import com.ultreon.libs.datetime.v0.Duration;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Notifications implements Renderable {
    private static final int HEIGHT = 80;
    private static final int WIDTH = 300;
    private static final int OFFSET = 20;
    private static final int GAP = 10;

    private final Lock lock = new ReentrantLock(true);
    private final UltracraftClient client;
    private final Deque<Notification> notifications = new ArrayDeque<>();
    private final Set<UUID> usedNotifications = new HashSet<>();

    public Notifications(UltracraftClient client) {
        this.client = client;
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        if (this.client.isLoading()) return;

        int x = this.client.getWidth() - Notifications.OFFSET - Notifications.WIDTH;
        int y = Notifications.OFFSET;

        this.lock.lock();
        this.notifications.removeIf(Notification::isDead);
        for (Notification notification : this.notifications) {
            String title = notification.getTitle();
            String summary = notification.getSummary();
            String subText = notification.getSubText();
            float motionRatio = notification.getMotion();
            int motion = (int) ((Notifications.WIDTH + Notifications.OFFSET) * motionRatio);

            renderer.fill(x + motion, y, Notifications.WIDTH, Notifications.HEIGHT, Color.rgb(0x101010));
            renderer.box(x + motion + 5, y + 5, Notifications.WIDTH - 10, Notifications.HEIGHT - 10, Color.rgb(0x505050));

            renderer.drawTextLeft(title, x + motion + 10, y + 13, Color.rgb(0xd0d0d0));
            renderer.drawTextLeft(summary, x + motion + 10, y + 40, Color.rgb(0xb0b0b0));
            renderer.drawTextLeft(subText == null ? "" : subText, x + motion + 10, y + 60, Color.rgb(0x707070));

            y += Notifications.HEIGHT + Notifications.GAP;
        }
        this.lock.unlock();
    }

    public void notify(Notification notification) {
        if (!UltracraftClient.isOnMainThread()) {
            UltracraftClient.invoke(() -> this.notify(notification));
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

    public void notify(String title, String description) {
        this.notify(Notification.builder(title, description).build());
    }

    public void notify(String title, String description, String subText) {
        this.notify(Notification.builder(title, description).subText(subText).build());
    }
}

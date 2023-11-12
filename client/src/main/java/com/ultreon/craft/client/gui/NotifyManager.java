package com.ultreon.craft.client.gui;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.icon.Icon;
import com.ultreon.craft.client.util.Renderable;
import com.ultreon.craft.text.MutableText;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.datetime.v0.Duration;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NotifyManager implements Renderable {
    private static final int HEIGHT = 41;
    private static final int WIDTH = 150;
    private static final int OFFSET = 5;
    private static final int GAP = 5;

    private final Lock lock = new ReentrantLock(true);
    private final UltracraftClient client;
    private final Deque<Notification> notifications = new ArrayDeque<>();
    private final Set<UUID> usedNotifications = new HashSet<>();
    private float motionY;

    public NotifyManager(UltracraftClient client) {
        this.client = client;
    }

    @Override
    public void render(Renderer renderer, int mouseX, int mouseY, float deltaTime) {
        if (this.client.isLoading()) return;

        int y = (int) (NotifyManager.OFFSET + this.motionY);

        this.lock.lock();
        this.notifications.removeIf(notification1 -> {
            if (notification1.isFinished()) {
                this.motionY += NotifyManager.HEIGHT + NotifyManager.GAP;
                return true;
            }
            return false;
        });

        for (Notification notification : this.notifications) {
            MutableText title = notification.getTitle().setBold(true);
            MutableText summary = notification.getSummary();
            MutableText subText = notification.getSubText();
            Icon icon = notification.getIcon();

            int width = NumberUtils.max(this.client.font.width(title), this.client.font.width(summary), this.client.font.width(subText), NotifyManager.WIDTH) + 10;
            if (icon != null) width += 37;

            int x = this.client.getScaledWidth() - NotifyManager.OFFSET - width;

            float motionRatio = notification.getMotion();
            int motion = (int) ((width + NotifyManager.OFFSET) * motionRatio);

            renderer.fill(x + motion + 1, y, width - 2, NotifyManager.HEIGHT, Color.rgb(0x101010));
            renderer.fill(x + motion, y + 1, width, NotifyManager.HEIGHT - 2, Color.rgb(0x101010));
            renderer.box(x + motion + 1, y + 1, width - 2, NotifyManager.HEIGHT - 2, Color.rgb(0x505050));

            if (icon != null) {
                icon.render(renderer, x + motion + 4, y + 4, 32, 32, deltaTime);
            }

            int textX = icon == null ? 0 : 37;

            renderer.drawTextLeft(title, x + motion + 5 + textX, y + 5, Color.rgb(0xd0d0d0));
            renderer.drawTextLeft(summary, x + motion + 5 + textX, y + 17, Color.rgb(0xb0b0b0));
            renderer.drawTextLeft(subText, x + motion + 5 + textX, y + 29, Color.rgb(0x707070));

            y += NotifyManager.HEIGHT + NotifyManager.GAP;
        }

        this.motionY = Math.max(this.motionY - ((deltaTime * NotifyManager.HEIGHT) * 4 + (deltaTime * this.motionY) * 4), 0);

        this.lock.unlock();
    }

    public void add(Notification notification) {
        if (!UltracraftClient.isOnMainThread()) {
            UltracraftClient.invoke(() -> this.add(notification));
            return;
        }

        this.notifications.addLast(notification);
    }

    public void add(String title, String description) {
        this.add(Notification.builder(title, description).build());
    }

    public void add(String title, String description, String subText) {
        this.add(Notification.builder(title, description).subText(subText).build());
    }

    public void add(MutableText title, MutableText description) {
        this.add(Notification.builder(title, description).build());
    }

    public void add(MutableText title, MutableText description, MutableText subText) {
        this.add(Notification.builder(title, description).subText(subText).build());
    }

    public void addOnce(UUID uuid, Notification message) {
        if (!this.usedNotifications.contains(uuid)) {
            this.usedNotifications.add(uuid);
            this.add(message);
        }
    }

    public void unavailable(String feature) {
        this.add(Notification.builder("Unavailable Feature", String.format("'%s' isn't available yet.", feature))
                .subText("Feature Locker")
                .duration(Duration.ofSeconds(5))
                .build()
        );
    }
}

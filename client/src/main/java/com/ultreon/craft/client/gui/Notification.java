package com.ultreon.craft.client.gui;

import com.ultreon.libs.datetime.v0.Duration;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.checkerframework.common.returnsreceiver.qual.This;

import java.util.Locale;

public class Notification {
    private static final int MAX_FADE_IN = 500;
    private static final int MAX_FADE_OUT = 500;
    private String title;
    private String summary;
    private final String subText;
    private final long duration;
    private long createTime = System.currentTimeMillis();
    private boolean sticky;

    private Notification(Builder builder) {
        this.title = builder.title;
        this.summary = builder.summary;
        this.subText = (builder.subText == null || builder.subText.isEmpty() ? "Game Notification" : builder.subText).toUpperCase(Locale.ROOT);
        this.duration = builder.duration.toMillis();
        this.sticky = builder.sticky;
    }

    public static Builder builder(String title, String summary) {
        return new Builder(title, summary);
    }

    public String getTitle() {
        return this.title;
    }

    public String getSummary() {
        return this.summary;
    }

    public String getSubText() {
        return this.subText;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public float getMotion() {
        if (this.getLifetime() < 0)
            return 1f;
        else if (this.getLifetime() < Notification.MAX_FADE_IN)
            return 1f - (float) this.getLifetime() / Notification.MAX_FADE_IN;
        else if (this.getLifetime() < Notification.MAX_FADE_IN + this.duration || this.sticky)
            return 0f;
        else if (this.getLifetime() < Notification.MAX_FADE_IN + this.duration + Notification.MAX_FADE_OUT)
            return (float) (this.getLifetime() - Notification.MAX_FADE_IN - this.duration) / Notification.MAX_FADE_OUT;
        else
            return 1f;
    }

    private long getCreateTime() {
        return this.createTime;
    }

    private long getLifetime() {
        return System.currentTimeMillis() - this.getCreateTime();
    }

    public boolean isDead() {
        if (this.sticky) return false;
        return this.getLifetime() > Notification.MAX_FADE_IN + this.duration + Notification.MAX_FADE_OUT;
    }

    public void set(String title, String summary) {
        this.createTime = System.currentTimeMillis();
        this.title = title;
        this.summary = summary;
        this.sticky = false;
    }

    public static class Builder {
        private final String title;
        private final String summary;
        private String subText = null;
        private Duration duration = Duration.ofSeconds(3);
        private boolean sticky = false;

        private Builder(String title, String summary) {
            this.title = title;
            this.summary = summary;
        }

        public @This Builder subText(String subText) {
            this.subText = subText;
            return this;
        }

        public @This Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public @This Builder sticky() {
            this.sticky = true;
            return this;
        }

        public @NewInstance Notification build() {
            return new Notification(this);
        }
    }
}

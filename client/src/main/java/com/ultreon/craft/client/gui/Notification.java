package com.ultreon.craft.client.gui;

import com.ultreon.craft.client.gui.icon.Icon;
import com.ultreon.craft.text.MutableText;
import com.ultreon.libs.datetime.v0.Duration;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.checkerframework.common.returnsreceiver.qual.This;

public class Notification {
    private static final int MAX_FADE_IN = 500;
    private static final int MAX_FADE_OUT = 500;
    private MutableText title;
    private MutableText summary;
    private final MutableText subText;
    private final long duration;
    private long createTime = System.currentTimeMillis();
    private boolean sticky;
    private Icon icon;

    private Notification(Builder builder) {
        this.title = builder.title;
        this.summary = builder.summary;
        this.subText = (builder.subText == null ? MutableText.literal("Game Notification") : builder.subText);
        this.duration = builder.duration.toMillis();
        this.sticky = builder.sticky;
        this.icon = builder.icon;
    }

    public static Builder builder(String title, String summary) {
        return new Builder(title, summary);
    }

    public static Builder builder(MutableText title, MutableText summary) {
        return new Builder(title, summary);
    }

    public MutableText getTitle() {
        return this.title;
    }

    public MutableText getSummary() {
        return this.summary;
    }

    public MutableText getSubText() {
        return this.subText;
    }

    public void setTitle(MutableText title) {
        this.title = title;
    }

    public void setSummary(MutableText summary) {
        this.summary = summary;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
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

    public boolean isFinished() {
        if (this.sticky) return false;
        return this.getLifetime() > Notification.MAX_FADE_IN + this.duration + Notification.MAX_FADE_OUT;
    }

    public void set(MutableText title, MutableText summary) {
        this.createTime = System.currentTimeMillis();
        this.title = title;
        this.summary = summary;
        this.sticky = false;
    }

    public Icon getIcon() {
        return this.icon;
    }

    public static class Builder {
        private final MutableText title;
        private final MutableText summary;
        private MutableText subText = null;
        private Duration duration = Duration.ofSeconds(3);
        private boolean sticky = false;
        public Icon icon = null;

        private Builder(String title, String summary) {
            this.title = MutableText.literal(title);
            this.summary = MutableText.literal(summary);
        }

        private Builder(MutableText title, MutableText summary) {
            this.title = title;
            this.summary = summary;
        }

        public @This Builder subText(String subText) {
            this.subText = MutableText.literal(subText);
            return this;
        }

        public @This Builder subText(MutableText subText) {
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

        public @This Builder icon(Icon icon) {
            this.icon = icon;
            return this;
        }

        public @NewInstance Notification build() {
            return new Notification(this);
        }
    }
}

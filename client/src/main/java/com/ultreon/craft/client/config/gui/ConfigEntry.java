package com.ultreon.craft.client.config.gui;

import com.google.common.base.Preconditions;
import com.ultreon.craft.client.config.Configuration;
import com.ultreon.libs.translations.v1.Language;
import org.jetbrains.annotations.Nullable;

public abstract class ConfigEntry<T> {
    private final String key;
    private T value;
    @Nullable
    private String comment;
    private final Configuration config;

    protected ConfigEntry(String key, T value, Configuration config) {
        this.key = key;
        this.value = value;
        this.config = config;
    }

    public T get() {
        return this.value;
    }

    public void set(T value) {
        Preconditions.checkNotNull(value, "Cannot set config value to null");
        this.value = value;
    }

    public ConfigEntry<T> comment(String comment) {
        Preconditions.checkNotNull(comment, "Cannot add null comment");
        this.comment = comment;
        return this;
    }

    protected abstract T read(String text);

    public void readAndSet(String text) {
        try {
            this.value = this.read(text);
        } catch (Exception ignored) {

        }
    }

    @Nullable
    public String getComment() {
        return this.comment;
    }

    public String getKey() {
        return this.key;
    }

    public String write() {
        return this.value.toString();
    }

    public String getDescription() {
        return Language.translate("config." + this.config.getId() + "." + this.getKey());
    }
}

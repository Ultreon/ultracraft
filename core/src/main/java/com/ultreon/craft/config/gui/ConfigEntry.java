package com.ultreon.craft.config.gui;

import com.google.common.base.Preconditions;
import com.ultreon.craft.config.Configuration;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.GuiComponent;
import com.ultreon.libs.translations.v1.Language;
import org.jetbrains.annotations.NotNull;

public abstract class ConfigEntry<T> {
    private final String key;
    private T value;
    private String comment;
    private final Configuration config;

    public ConfigEntry(String key, T value, Configuration config) {
        this.key = key;
        this.value = value;
        this.config = config;
    }

    public T get() {
        return this.value;
    }

    public void set(@NotNull T value) {
        Preconditions.checkNotNull(value, "Entry value shouldn't be null.");
        this.value = value;
    }

    public ConfigEntry<T> comment(String comment) {
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

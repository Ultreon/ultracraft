package com.ultreon.craft.client.config;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.config.gui.ConfigEntry;
import com.ultreon.libs.translations.v1.LanguageManager;

import java.util.Locale;

public class GameSettings extends Configuration {
    public final ConfigEntry<Locale> language;
    public final ConfigEntry<Integer> renderDistance;
    public final ConfigEntry<Boolean> fullscreen;
    public final ConfigEntry<Boolean> craftingShowOnlyCraftable;

    public GameSettings() {
        super();

        this.language = this.add("language", Locale.of("en", "US"), Locale::forLanguageTag, Locale::toLanguageTag, "The preferred language");
        this.renderDistance = this.add("renderDistance", 16, 2, 24, "The maximum distance to show the world in chunks (16x16)");
        this.fullscreen = this.add("fullscreen", false, "Enable fullscreen mode");
        this.craftingShowOnlyCraftable = this.add("craftingShowOnlyCraftable", true, "Show only craftable recipes");
    }

    @Override
    public void save() {
        super.save();

        this.reloadLanguage();
    }

    public void reloadLanguage() {
        LanguageManager.setCurrentLanguage(this.language.get());
        if (this.fullscreen.get()) {
            UltracraftClient.get().setFullScreen(true);
        }
    }
}

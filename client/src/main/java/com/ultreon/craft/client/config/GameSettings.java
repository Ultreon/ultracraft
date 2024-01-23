package com.ultreon.craft.client.config;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.config.gui.ConfigEntry;
import com.ultreon.craft.client.text.LanguageManager;

import java.util.Locale;

public class GameSettings extends Configuration {
    public final ConfigEntry<Locale> language;
    public final ConfigEntry<Integer> renderDistance;
    public final ConfigEntry<Boolean> fullscreen;
    public final ConfigEntry<Boolean> craftingShowOnlyCraftable;
    public final ConfigEntry<Integer> guiScale;
    public final ConfigEntry<Boolean> hidePlayerWhenThirdPerson;
    public final ConfigEntry<Integer> width;
    public final ConfigEntry<Integer> height;

    public GameSettings() {
        super();

        this.language = this.add("language", Locale.of("en", "US"), Locale::forLanguageTag, Locale::toLanguageTag, "The preferred language");
        this.renderDistance = this.add("renderDistance", 16, 2, 24, "The maximum distance to show the world in chunks (16x16)");
        this.fullscreen = this.add("fullscreen", false, "Enable fullscreen mode");
        this.craftingShowOnlyCraftable = this.add("craftingShowOnlyCraftable", true, "Show only craftable recipes");
        this.guiScale = this.add("guiScale", 2, 0, 4, "The GUI scale: 0 = automatic scale, 1 = 1x pixel size, 2 = 2x pixel size, etc.");
        this.hidePlayerWhenThirdPerson = this.add("hidePlayerWhenThirdPerson", false, "Hide the player when third-person view is enabled");
        this.width = this.add("width", 1280, 128, 122880, "The width of the window");
        this.height = this.add("height", 720, 64, 69120, "The height of the window");
    }

    @Override
    public void save() {
        super.save();

        this.onChanged();
    }

    public void onChanged() {
        LanguageManager.setCurrentLanguage(this.language.get());
        UltracraftClient.get().setFullScreen(this.fullscreen.get());
    }
}

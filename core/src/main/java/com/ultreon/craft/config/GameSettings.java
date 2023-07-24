package com.ultreon.craft.config;

import com.ultreon.craft.GamePlatform;
import com.ultreon.craft.config.gui.ConfigEntry;
import com.ultreon.libs.translations.v1.LanguageManager;

import java.util.Locale;

public class GameSettings extends Configuration {
    public final ConfigEntry<Locale> language;
    public final ConfigEntry<Integer> renderDistance;

    public GameSettings() {
        super();

        this.language = this.add("language", new Locale("en"), Locale::new, Locale::getLanguage, "The preferred language");
        this.renderDistance = this.add("renderDistance", this.getDefaultRenderDistance(), 2, 12, "The maximum distance to show the world in chunks (16x16)");
    }

    private int getDefaultRenderDistance() {
        switch (GamePlatform.instance.getPlatformType()) {
            case DESKTOP:
                return 8;
            case MOBILE:
            case WEB:
                return 4;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void save() {
        super.save();

        this.reloadLanguage();
    }

    public void reloadLanguage() {
        LanguageManager.setCurrentLanguage(this.language.get());
    }
}

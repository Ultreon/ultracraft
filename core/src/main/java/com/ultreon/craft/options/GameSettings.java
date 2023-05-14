package com.ultreon.craft.options;

import com.ultreon.libs.translations.v0.LanguageManager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

public class GameSettings {
    public static final String FILENAME = "settings.xml";
    private Locale language;

    public GameSettings() {
        var props = load();
        this.language = new Locale(props.getProperty("language", "en"));

        System.out.println("language = " + language);
        LanguageManager.setCurrentLanguage(language);
        save();
    }

    @SuppressWarnings("BlockingMethodInNonBlockingContext")
    private Properties load() {
        Properties properties = new Properties();
        try {
            properties.loadFromXML(new FileInputStream(FILENAME));
        } catch (IOException e) {
            return new Properties();
        }
        return properties;
    }

    public GameSettings(Properties props) {
    }

    public Locale getLanguage() {
        return this.language;
    }

    public void setLanguage(Locale language) {
        this.language = language;
        LanguageManager.setCurrentLanguage(language);
        save();
    }

    @SuppressWarnings("BlockingMethodInNonBlockingContext")
    private void save() {
        var props = new Properties();
        props.setProperty("language", language.getLanguage());
        try {
            props.storeToXML(new FileOutputStream(FILENAME), "This is the settings for the Ultreon Craft game.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

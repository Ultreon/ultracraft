package com.ultreon.craft.options;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.ultreon.libs.translations.v0.LanguageManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

public class GameSettings {
    public static final String FILENAME = "settings.xml";
    private Locale language;
    private int renderDistance;

    public GameSettings() {
        var props = load();
        this.language = new Locale(props.getProperty("language", "en"));
        this.renderDistance = getInt(props, "renderDistance", 8);

        LanguageManager.setCurrentLanguage(language);
        save();
    }

    private int getInt(Properties props, String key, int defaultValue) {
        try {
            return Integer.parseInt(props.getProperty(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
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

    public int getRenderDistance() {
        return renderDistance;
    }

    public void setRenderDistance(int renderDistance) {
        this.renderDistance = renderDistance;
        save();
    }

    @SuppressWarnings("BlockingMethodInNonBlockingContext")
    private void save() {
        var props = new Properties();
        props.setProperty("language", language.getLanguage());
        props.setProperty("renderDistance", Integer.toString(renderDistance));
        try {
            FileHandle external = Gdx.files.external("settings.xml");
            props.storeToXML(external.write(false), "This is the settings for the Ultreon Craft game.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

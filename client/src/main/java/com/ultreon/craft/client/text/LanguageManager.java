package com.ultreon.craft.client.text;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ultreon.craft.registry.Registry;
import com.ultreon.craft.resources.ResourceManager;
import com.ultreon.craft.util.Identifier;
import com.ultreon.libs.commons.v0.Logger;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LanguageManager {
    public static final LanguageManager INSTANCE = new LanguageManager();
    public static final Registry<Language> REGISTRY = Registry.<Language>builder(new Identifier("languages")).build();
    private static Locale currentLanguage;
    private final Map<Locale, Language> languages = new HashMap<>();
    private final Set<Locale> locales = new HashSet<>();
    private final Set<Identifier> ids = new HashSet<>();
    private final Map<Locale, Identifier> locale2id = new HashMap<>();
    private final Map<Identifier, Locale> id2locale = new HashMap<>();
    private Logger logger = (level, message, t) -> {};

    private LanguageManager() {

    }

    public static Locale getCurrentLanguage() {
        return currentLanguage;
    }

    public static void setCurrentLanguage(Locale currentLanguage) {
        LanguageManager.currentLanguage = currentLanguage;
    }

    public Language load(Locale locale, Identifier id, ResourceManager resourceManager) {
        Gson gson = new Gson();
        String newPath = "languages/" + id.path() + ".json";
        List<byte[]> assets = resourceManager.getAllDataById(id.withPath(newPath));
        Map<String, String> languageMap = new HashMap<>();
        for (byte[] asset : assets) {
            JsonObject object = gson.fromJson(new StringReader(new String(asset, StandardCharsets.UTF_8)), JsonObject.class);
            this.loadFile(languageMap, object);
        }

        Language language = new Language(locale, languageMap, id);
        this.languages.put(locale, language);
        REGISTRY.register(id, language);
        return language;
    }

    public Language load(Locale locale, Identifier id, Reader reader) {
        Gson gson = new Gson();
        Map<String, String> languageMap = new HashMap<>();
        this.loadFile(languageMap, gson.fromJson(reader, JsonObject.class));
        Language language = new Language(locale, languageMap, id);
        this.languages.put(locale, language);
        REGISTRY.register(id, language);
        return language;
    }

    public Language get(Locale locale) {
        return this.languages.get(locale);
    }

    public void register(Locale locale, Identifier id) {
        if (this.locales.contains(locale)) {
            this.getLogger().warn("Locale overridden: " + locale.getLanguage());
        }
        if (this.ids.contains(id)) {
            this.getLogger().warn("LanguageID overridden: " + id);
        }

        this.locales.add(locale);
        this.ids.add(id);
        this.locale2id.put(locale, id);
        this.id2locale.put(id, locale);
    }

    public Locale getLocale(Identifier id) {
        return this.id2locale.get(id);
    }

    public Identifier getLanguageID(Locale locale) {
        return this.locale2id.get(locale);
    }

    private void loadFile(Map<String, String> languageMap, JsonObject object) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonElement value = entry.getValue();
            String key = entry.getKey();
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                languageMap.put(key, value.getAsString());
            }
        }
    }

    public Set<Locale> getLocales() {
        return new HashSet<>(this.locales);
    }

    public Set<Identifier> getLanguageIDs() {
        return new HashSet<>(this.ids);
    }

    public List<Language> getLanguages() {
        return new ArrayList<>(this.languages.values());
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}

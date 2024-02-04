package com.ultreon.craft.text;

import com.ultreon.craft.util.ElementID;

import java.util.Locale;
import java.util.Map;

public class ServerLanguage {
    private final Locale locale;
    private final Map<String, String> languageMap;
    private final ElementID id;

    public ServerLanguage(Locale locale, Map<String, String> languageMap, ElementID id) {
        this.locale = locale;
        this.languageMap = languageMap;
        this.id = id;
    }

    public String get(String path, Object... args) {
        String[] split = path.split("/");

        String s = this.languageMap.get(path);
        return s == null ? null : String.format(s, args);
    }

    public Locale getLocale() {
        return this.locale;
    }

    public ElementID getId() {
        return this.id;
    }
}

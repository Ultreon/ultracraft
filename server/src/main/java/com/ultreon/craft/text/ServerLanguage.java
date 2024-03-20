package com.ultreon.craft.text;

import com.ultreon.craft.util.Identifier;

import java.util.Locale;
import java.util.Map;

public class ServerLanguage {
    private final Locale locale;
    private final Map<String, String> languageMap;
    private final Identifier id;

    public ServerLanguage(Locale locale, Map<String, String> languageMap, Identifier id) {
        this.locale = locale;
        this.languageMap = languageMap;
        this.id = id;
    }

    public String get(String path, Object... args) {
        String s = this.languageMap.get(path);
        return s == null ? null : String.format(s, args);
    }

    public Locale getLocale() {
        return this.locale;
    }

    public Identifier getId() {
        return this.id;
    }
}

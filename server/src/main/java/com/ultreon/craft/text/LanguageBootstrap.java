package com.ultreon.craft.text;

import com.ultreon.craft.util.LazyValue;

public interface LanguageBootstrap {
    LazyValue<LanguageBootstrap> bootstrap = new LazyValue<>();

    String handleTranslation(String path, Object... args);

    static String translate(String path, Object... args) {
        if (!bootstrap.isInitialized()) return path;
        return bootstrap.get().handleTranslation(path, args);
    }
}

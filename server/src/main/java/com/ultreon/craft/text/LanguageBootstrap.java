package com.ultreon.craft.text;

import com.ultreon.craft.util.LazyValue;

/**
 * Interface for language bootstrap
 */
public interface LanguageBootstrap {
    /**
     * Lazily initialized value for language bootstrap
     */
    LazyValue<LanguageBootstrap> bootstrap = new LazyValue<>();

    /**
     * Handles translation for the given path and arguments
     *
     * @param path the translation path
     * @param args the arguments for the translation
     * @return the translated string
     */
    String handleTranslation(String path, Object... args);

    /**
     * Translates the given path and arguments
     *
     * @param path the translation path
     * @param args the arguments for the translation
     * @return the translated string
     */
    static String translate(String path, Object... args) {
        if (!bootstrap.isInitialized()) return path;
        return bootstrap.get().handleTranslation(path, args);
    }
}

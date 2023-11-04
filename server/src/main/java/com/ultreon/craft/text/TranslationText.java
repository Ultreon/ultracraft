package com.ultreon.craft.text;

import com.ultreon.libs.translations.v1.Language;

public class TranslationText extends MutableText {
    private final String path;
    private final Object[] args;

    TranslationText(String path, Object... args) {
        this.path = path;
        this.args = args;
    }

    @Override
    protected String createString() {
        return Language.translate(this.path, this.args);
    }
}

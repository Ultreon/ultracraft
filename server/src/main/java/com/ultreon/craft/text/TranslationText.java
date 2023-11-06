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
    public String createString() {
        return Language.translate(this.path, this.args);
    }

    @Override
    public MutableText copy() {
        var copy = this.extras.stream().map(TextObject::copy).toList();
        var translationText = new TranslationText(this.path, this.args);
        translationText.extras.addAll(copy);
        return translationText;
    }
}

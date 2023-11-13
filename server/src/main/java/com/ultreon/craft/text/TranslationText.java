package com.ultreon.craft.text;

import com.ultreon.libs.translations.v1.Language;
import org.jetbrains.annotations.NotNull;

public class TranslationText extends MutableText {
    private final @NotNull String path;
    private final Object @NotNull [] args;

    TranslationText(@NotNull String path, Object @NotNull ... args) {
        this.path = path;
        this.args = args;
    }

    @Override
    public @NotNull String createString() {
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

package com.ultreon.craft.text;

import com.ultreon.libs.translations.v1.Language;

public interface Translatable {
    String getTranslationId();

    default MutableText getTranslation() {
        return TextObject.translation(this.getTranslationId());
    }

    default String getTranslationText() {
        return Language.translate(this.getTranslationId());
    }
}

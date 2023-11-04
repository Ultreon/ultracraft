package com.ultreon.craft.text;

import com.ultreon.libs.translations.v1.Language;

public interface Translatable {
    String getTranslationPath();

    default MutableText getTranslation() {
        return TextObject.translation(this.getTranslationPath());
    }

    default String getTranslationText() {
        return Language.translate(this.getTranslationPath());
    }
}

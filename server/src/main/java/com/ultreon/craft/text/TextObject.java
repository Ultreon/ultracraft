package com.ultreon.craft.text;

import org.jetbrains.annotations.Nullable;

import java.text.AttributedString;

public abstract class TextObject {
    public static final TextObject EMPTY = new TextObject() {
        @Override
        protected String createString() {
            return "";
        }

        @Override
        public AttributedString getAttrString() {
            return new AttributedString("");
        }
    };

    protected abstract String createString();

    public String getText() {
        return this.createString();
    }

    public abstract AttributedString getAttrString();

    public static MutableText literal(String text) {
        return new LiteralText(text);
    }

    public static MutableText translation(String path, Object... args) {
        return new TranslationText(path, args);
    }

    public static TextObject nullToEmpty(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return EMPTY;
        }
        return TextObject.literal(text);
    }
}

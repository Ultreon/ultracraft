package com.ultreon.craft.text;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public abstract class TextObject implements Iterable<TextObject> {
    public abstract @NotNull String createString();

    public String getText() {
        return this.createString();
    }

    public static TextObject empty() {
        return new TextObject() {
            @Override
            public @NotNull String createString() {
                return "";
            }

            @Override
            public MutableText copy() {
                return new LiteralText(this.createString());
            }
        };
    }

    public static LiteralText literal(@Nullable String text) {
        return new LiteralText(text != null ? text : "");
    }

    public static TranslationText translation(String path, Object... args) {
        return new TranslationText(path, args);
    }

    public static TextObject nullToEmpty(@Nullable String text) {
        return text == null ? TextObject.empty() : TextObject.literal(text);
    }

    public abstract MutableText copy();

    @Override
    public @NotNull Iterator<TextObject> iterator() {
        return Iterators.singletonIterator(this);
    }
}

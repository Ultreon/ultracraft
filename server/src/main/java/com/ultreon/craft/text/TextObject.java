package com.ultreon.craft.text;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public abstract class TextObject implements Iterable<TextObject> {
    public abstract String createString();

    public String getText() {
        return this.createString();
    }

    public static MutableText empty() {
        return new LiteralText("");
    }

    public static LiteralText literal(String text) {
        return new LiteralText(text);
    }

    public static TranslationText translation(String path, Object... args) {
        return new TranslationText(path, args);
    }

    public static MutableText nullToEmpty(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return TextObject.empty();
        }
        return TextObject.literal(text);
    }

    public abstract MutableText copy();

    @Override
    public @NotNull Iterator<TextObject> iterator() {
        return Iterators.singletonIterator(this);
    }
}

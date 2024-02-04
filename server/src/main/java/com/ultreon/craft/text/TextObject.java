package com.ultreon.craft.text;

import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.stream.Stream;

public abstract class TextObject implements Iterable<TextObject> {
    public static TextObject deserialize(MapType data) {
        String type = data.getString("type");
        return switch (type) {
            case "literal" -> LiteralText.deserialize(data);
            case "translation" -> TranslationText.deserialize(data);
            case "empty" -> TextObject.empty();
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public abstract @NotNull String createString();

    public String getText() {
        return this.createString();
    }

    public abstract MapType serialize();

    public static TextObject empty() {
        return new TextObject() {
            @Override
            public @NotNull String createString() {
                return "";
            }

            @Override
            public MapType serialize() {
                MapType data = new MapType();
                data.putString("type", "empty");
                return data;
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
        return this.stream().iterator();
    }

    protected Stream<TextObject> stream() {
        return Stream.of(this);
    }
}

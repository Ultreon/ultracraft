package com.ultreon.craft.text;

import org.jetbrains.annotations.NotNull;

public class LiteralText extends MutableText {
    private final @NotNull String text;

    LiteralText(@NotNull String text) {
        this.text = text;
    }

    @Override
    public @NotNull String createString() {
        return this.text;
    }

    @Override
    public LiteralText copy() {
        var copy = this.extras.stream().map(TextObject::copy).toList();
        var literalText = new LiteralText(this.text);
        literalText.extras.addAll(copy);
        return literalText;
    }
}

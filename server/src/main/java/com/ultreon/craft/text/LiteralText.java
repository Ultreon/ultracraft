package com.ultreon.craft.text;

public class LiteralText extends MutableText {
    private final String text;

    LiteralText(String text) {
        this.text = text;
    }

    @Override
    public String createString() {
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

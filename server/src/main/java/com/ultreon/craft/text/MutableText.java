package com.ultreon.craft.text;

import com.google.common.base.Preconditions;
import com.ultreon.craft.util.Color;
import org.checkerframework.common.reflection.qual.NewInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class MutableText extends TextObject {
    List<TextObject> extras = new ArrayList<>();
    TextStyle style = new TextStyle();

    protected MutableText() {

    }

    @Override
    public final String getText() {
        var builder = new StringBuilder();
        builder.append(this.createString());
        for (var extra : this.extras) {
            builder.append(extra.getText());
        }
        return builder.toString();
    }

    public MutableText style(Consumer<TextStyle> consumer) {
        consumer.accept(this.style);
        return this;
    }

    public Color getColor() {
        return this.style.getColor();
    }

    public MutableText setColor(Color color) {
        this.style.color(color);
        return this;
    }

    public boolean isUnderlined() {
        return this.style.isUnderline();
    }

    public MutableText setUnderlined(boolean underlined) {
        this.style.underline(underlined);
        return this;
    }

    public boolean isStrikethrough() {
        return this.style.isStrikethrough();
    }

    public MutableText setStrikethrough(boolean strikethrough) {
        this.style.strikethrough(strikethrough);
        return this;
    }

    public boolean isBold() {
        return this.style.isBold();
    }

    public MutableText setBold(boolean bold) {
        this.style.bold(bold);
        return this;
    }

    public boolean isItalic() {
        return this.style.isItalic();
    }

    public MutableText setItalic(boolean italic) {
        this.style.italic(italic);
        return this;
    }

    public int getSize() {
        return this.style.getSize();
    }

    public void setSize(int size) {
        this.style.size(size);
    }

    /**
     * Appends a TextObject to the current TextObject, creating a new instance
     * <b>WARNING: <i>This action is performance intensive, not recommended to use within loops.</i></b>
     *
     * @param append The TextObject to append
     * @return A new instance of MutableText
     */
    public @NewInstance MutableText append(TextObject append) {
        Preconditions.checkNotNull(append, "Text object cannot be null");
        this.extras.add(append);
        return this;
    }

    public MutableText append(String text) {
        return this.append(TextObject.nullToEmpty(text));
    }

    public MutableText append(Object o) {
        return this.append(TextObject.nullToEmpty(String.valueOf(o)));
    }

    @Override
    public abstract MutableText copy();

    @Override
    protected Stream<TextObject> stream() {
        var builder = new ArrayList<TextObject>();
        builder.add(this);
        for (var extra : this.extras) {
            builder.addAll(extra.stream().toList());
        }
        return builder.stream();
    }
}

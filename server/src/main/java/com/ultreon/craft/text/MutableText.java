package com.ultreon.craft.text;

import com.google.common.base.Preconditions;
import com.ultreon.craft.util.Color;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class MutableText extends TextObject {
    final List<TextObject> extras = new ArrayList<>();
    private Color color;
    private int size;
    private boolean underlined = false;
    private boolean bold = false;
    private boolean italic = false;
    private boolean strikethrough = false;

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

    public Color getColor() {
        return this.color;
    }

    public MutableText setColor(Color color) {
        this.color = color;
        return this;
    }

    public boolean isUnderlined() {
        return this.underlined;
    }

    public MutableText setUnderlined(boolean underlined) {
        this.underlined = underlined;
        return this;
    }

    public boolean isStrikethrough() {
        return this.strikethrough;
    }

    public MutableText setStrikethrough(boolean strikethrough) {
        this.strikethrough = strikethrough;
        return this;
    }

    public boolean isBold() {
        return this.bold;
    }

    public MutableText setBold(boolean bold) {
        this.bold = bold;
        return this;
    }

    public boolean isItalic() {
        return this.italic;
    }

    public MutableText setItalic(boolean italic) {
        this.italic = italic;
        return this;
    }

    public int getSize() {
        return this.size;
    }

    public MutableText setSize(int size) {
        this.size = size;
        return this;
    }

    public MutableText append(TextObject append) {
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

    public abstract MutableText copy();

    @Override
    public @NotNull Iterator<TextObject> iterator() {
        return new MutableTextIterator();
    }

    private class MutableTextIterator implements Iterator<TextObject> {
        private int index = -1;
        private TextObject next = MutableText.this;

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public TextObject next() {
            if (this.next == null) {
                throw new NoSuchElementException("No more elements in the iterator");
            }

            this.index++;
            TextObject next1 = this.next;
            this.next = this.index >= MutableText.this.extras.size() ? null : MutableText.this.extras.get(this.index);
            return next1;
        }
    }
}

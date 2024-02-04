package com.ultreon.craft.client.gui.widget;

import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.Position;
import com.ultreon.craft.text.MutableText;
import com.ultreon.craft.text.TextObject;
import com.ultreon.libs.commons.v0.Mth;
import org.checkerframework.common.value.qual.IntRange;

import java.util.function.Function;
import java.util.function.Supplier;

public class CycleButton<T extends Enum<T>> extends TextButton {
    private TextObject label = TextObject.empty();
    private T[] values;
    private Function<T, TextObject> formatter;
    private int cur;

    public CycleButton(int x, int y, @IntRange(from = 21) int width, MutableText name) {
        super(width);

        this.label = name;
        this.text().set(name);
    }

    public CycleButton(int x, int y, @IntRange(from = 21) int width, @IntRange(from = 21) int height, MutableText name) {
        super(width, height);

        this.label = name;
        this.text().set(name);
    }

    public CycleButton() {
        super(200, 21);
    }

    @SafeVarargs
    public final CycleButton<T> values(T... values) {
        this.values = values;
        return this;
    }

    public final CycleButton<T> formatter(Function<T, TextObject> formatter) {
        this.formatter = formatter;
        return this;
    }

    @Override
    public CycleButton<T> position(Supplier<Position> position) {
        this.onRevalidate(widget -> this.setPos(position.get()));
        return this;
    }

    @Override
    public CycleButton<T> bounds(Supplier<Bounds> position) {
        this.onRevalidate(widget -> this.setBounds(position.get()));
        return this;
    }

    @Override
    public boolean click() {
        this.cur = (this.cur + 1) % this.values.length;
        this.text().set(this.label.copy().append(": ").append(this.formatter.apply(this.values[this.cur])));
        return true;
    }

    public T getValue() {
        return this.values[this.cur];
    }

    public int getIndex() {
        return this.cur;
    }

    public TextObject getLabel() {
        return this.label;
    }

    public String getRawLabel() {
        return this.label.getText();
    }

    public CycleButton<T> index(int index) {
        this.cur = Mth.clamp(index, 0, this.values.length - 1);
        return this;
    }

    public CycleButton<T> value(T o) {
        this.cur = o.ordinal();
        return this;
    }

    public CycleButton<T> label(TextObject label) {
        this.label = label;
        return this;
    }

    public CycleButton<T> label(String label) {
        this.label = TextObject.literal(label);
        return this;
    }

    public CycleButton<T> labelTranslation(String label, Object... args) {
        this.label = TextObject.translation(label, args);
        return this;
    }
}

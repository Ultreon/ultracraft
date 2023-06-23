package com.ultreon.craft.render.gui;

import com.ultreon.craft.render.gui.widget.Button;
import com.ultreon.libs.commons.v0.Mth;
import org.checkerframework.common.value.qual.IntRange;

import java.util.function.Function;

public class CycleButton<T> extends Button {
    private final String name;
    private T[] values;
    private Function<T, String> formatter;
    private int cur;

    public CycleButton(int x, int y, @IntRange(from = 21) int width, String name) {
        super(x, y, width, name);

        this.name = name;
    }

    public CycleButton(int x, int y, @IntRange(from = 21) int width, @IntRange(from = 21) int height, String name) {
        super(x, y, width, height, name);

        this.name = name;
    }

    @SafeVarargs
    public final CycleButton<T> withValues(T... values) {
        this.values = values;
        return this;
    }

    public final CycleButton<T> withFormatter(Function<T, String> formatter) {
        this.formatter = formatter;
        return this;
    }

    @Override
    public boolean click() {
        this.cur = (this.cur + 1) % this.values.length;
        this.setMessage(this.name + ": " + this.formatter.apply(this.values[this.cur]));
        return true;
    }

    public T getValue() {
        return this.values[this.cur];
    }

    public int getIndex() {
        return this.cur;
    }

    public void setIndex(int index) {
        this.cur = Mth.clamp(index, 0, this.values.length - 1);
    }
}

package com.ultreon.craft.api.commands;

import com.ultreon.libs.collections.v0.iterator.IntIterable;
import com.ultreon.libs.collections.v0.iterator.IntIterator;
import org.jetbrains.annotations.NotNull;

public class Range implements IntIterable {
    private final int start;
    private final int end;
    private final int step;

    public Range(int value) {
        this(value, value);
    }

    public Range(int start, int end) {
        this(start, end, 1);
    }

    public Range(int start, int end, int step) {
        this.start = start;
        this.end = end;
        this.step = step;
    }

    @Override
    public @NotNull IntIterator iterator() {
        return new IntIterator() {
            private int index = Range.this.start;

            @Override
            public boolean hasNext() {
                return this.index < Range.this.end;
            }

            @Override
            public int nextInt() {
                this.index += Range.this.step;
                return this.index;
            }
        };
    }
}
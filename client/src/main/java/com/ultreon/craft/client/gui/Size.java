package com.ultreon.craft.client.gui;

import com.ultreon.craft.util.Copyable;
import org.checkerframework.common.returnsreceiver.qual.This;

import java.util.Objects;

public class Size implements Copyable<Size> {
    public int width;
    public int height;

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Size(int size) {
        this.width = size;
        this.height = size;
    }

    public Size() {
        this(0, 0);
    }

    public @This Size set(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public @This Size set(int size) {
        this.width = size;
        this.height = size;
        return this;
    }

    public @This Size set(Size size) {
        this.width = size.width;
        this.height = size.height;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Size size = (Size) o;
        return this.width == size.width && this.height == size.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.width, this.height);
    }

    @Override
    public String toString() {
        return "(" + this.width + " x " + this.height + ")";
    }

    public Size cpy() {
        return new Size(this.width, this.height);
    }

    public void idt() {
        this.width = 0;
        this.height = 0;
    }
}

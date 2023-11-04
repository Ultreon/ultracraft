package com.ultreon.craft.client.gui;

import java.util.Objects;
import java.util.function.IntSupplier;

public class LayoutBounds {
    private final IntSupplier x;
    private final IntSupplier y;
    private final IntSupplier width;
    private final IntSupplier height;
    private final Bounds current = new Bounds();

    public LayoutBounds(IntSupplier x, IntSupplier y, IntSupplier width, IntSupplier height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int currentX() {
        return this.current.pos().x;
    }

    public int currentY() {
        return this.current.pos().y;
    }

    public int currentWidth() {
        return this.current.size().width;
    }

    public int currentHeight() {
        return this.current.size().height;
    }

    public Bounds currentBounds() {
        return this.current.cpy();
    }

    public void setCurrentX(int x) {
        this.current.pos().x = x;
    }

    public void setCurrentY(int y) {
        this.current.pos().y = y;
    }

    public void setCurrentWidth(int width) {
        this.current.size().width = width;
    }

    public void setCurrentHeight(int height) {
        this.current.size().height = height;
    }

    public void setCurrentBounds(int x, int y, int width, int height) {
        this.current.setBounds(x, y, width, height);
    }

    public void setCurrentBounds(Bounds bounds) {
        this.current.setBounds(bounds);
    }

    public void revalidate() {
        this.current.pos().x = this.x.getAsInt();
        this.current.pos().y = this.y.getAsInt();
        this.current.size().width = this.width.getAsInt();
        this.current.size().height = this.height.getAsInt();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        LayoutBounds that = (LayoutBounds) o;
        return Objects.equals(this.x, that.x) && Objects.equals(this.y, that.y) && Objects.equals(this.width, that.width) && Objects.equals(this.height, that.height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y, this.width, this.height);
    }

    @Override
    public String toString() {
        return "(" + this.x.getAsInt() + ", " + this.y.getAsInt() + "):(" + this.width.getAsInt() + " x " + this.height.getAsInt() + ")";
    }
}

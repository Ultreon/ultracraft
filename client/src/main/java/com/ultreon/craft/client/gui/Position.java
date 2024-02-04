package com.ultreon.craft.client.gui;

import com.ultreon.craft.util.Copyable;

import java.util.Objects;

public class Position implements Copyable<Position> {
    public int x;
    public int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(int value) {
        this.x = value;
        this.y = value;
    }

    public Position() {
        this(0, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return this.x == position.x && this.y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }

    @Override
    public Position cpy() {
        return new Position(this.x, this.y);
    }

    public void set(Position pos) {
        this.x = pos.x;
        this.y = pos.y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void idt() {
        this.x = 0;
        this.y = 0;
    }
}

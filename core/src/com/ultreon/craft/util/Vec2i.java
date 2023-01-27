package com.ultreon.craft.util;

import com.badlogic.gdx.math.Vector2;

public class Vec2i {
    public int x;
    public int y;

    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vec2i(Vec2i vec2i) {
        this.x = vec2i.x;
        this.y = vec2i.y;
    }

    public static Vec2i roundToInt(Vector2 vector2) {
        return new Vec2i(Math.round(vector2.x), Math.round(vector2.y));
    }

    public Vec2i add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vec2i sub(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vec2i mul(int x, int y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Vec2i div(int x, int y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    public Vec2i rem(int x, int y) {
        this.x %= x;
        this.y %= y;
        return this;
    }
}

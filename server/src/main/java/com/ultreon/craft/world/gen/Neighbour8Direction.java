package com.ultreon.craft.world.gen;

import com.ultreon.libs.commons.v0.vector.Vec2i;

public enum Neighbour8Direction {
    N(1, 0),
    NE(1, 1),
    E(1, 0),
    SE(-1, 1),
    S(-1, 0),
    SW(-1, -1),
    W(0, -1),
    NW(1, -1);

    public final int x;
    public final int y;

    Neighbour8Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vec2i vec() {
        return new Vec2i(this.x, this.y);
    }
}

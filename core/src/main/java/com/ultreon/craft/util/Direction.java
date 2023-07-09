package com.ultreon.craft.util;

import com.ultreon.libs.commons.v0.vector.Vec3i;

public enum Direction {
    EAST(new Vec3i(-1, 0, 0), Axis.X),
    WEST(new Vec3i(1, 0, 0), Axis.X),
    DOWN(new Vec3i(0, -1, 0), Axis.Y),
    UP(new Vec3i(0, 1, 0), Axis.Y),
    NORTH(new Vec3i(0, 0, -1), Axis.Z),
    SOUTH(new Vec3i(0, 0, 1), Axis.Z);

    private final Vec3i offset;
    private final Axis axis;

    Direction(Vec3i offset, Axis axis) {
        this.offset = offset;
        this.axis = axis;
    }

    public Vec3i getOffset() {
        return this.offset;
    }

    public Axis getAxis() {
        return this.axis;
    }

    public enum Axis {
        X, Y, Z
    }
}

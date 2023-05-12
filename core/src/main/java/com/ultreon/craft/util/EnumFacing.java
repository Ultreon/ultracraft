package com.ultreon.craft.util;

import com.badlogic.gdx.math.Vector3;
import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum EnumFacing {
    DOWN(0, -1, 0, EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Y),
    UP(0, 1, 0, EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Y),
    NORTH(0, 0, -1, EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.Z),
    SOUTH(0, 0, 1, EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.Z),
    WEST(-1, 0, 0, EnumFacing.AxisDirection.NEGATIVE, EnumFacing.Axis.X),
    EAST(1, 0, 0, EnumFacing.AxisDirection.POSITIVE, EnumFacing.Axis.X);

    private final int x;
    private final int y;
    private final int z;
    private final EnumFacing.AxisDirection axisDirection;
    private final EnumFacing.Axis axis;

    EnumFacing(int x, int y, int z, EnumFacing.AxisDirection axisDirection, EnumFacing.Axis axis) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.axisDirection = axisDirection;
        this.axis = axis;
    }

    public static EnumFacing byAxis(Axis axis, AxisDirection axisDirection) {
        Preconditions.checkNotNull(axis, "The 'axis' parameter is null.");
        Preconditions.checkNotNull(axisDirection, "The 'axisDirection' parameter is null.");

        for (EnumFacing facing : EnumFacing.values()) {
            if (facing.getAxis() == axis && facing.getAxisDirection() == axisDirection) {
                return facing;
            }
        }
        throw new Error("Can't find facing.");
    }

    public int getOffsetX() {
        return this.x;
    }

    public int getOffsetY() {
        return this.y;
    }

    public int getOffsetZ() {
        return this.z;
    }

    public EnumFacing.Axis getAxis() {
        return this.axis;
    }

    public EnumFacing.AxisDirection getAxisDirection() {
        return this.axisDirection;
    }

    public Vector3 getDirectionVec() {
        return new Vector3(this.x, this.y, this.z);
    }

    public enum Axis implements Predicate<EnumFacing>, Named {
        X("x") {
            public int getCoordinate(int x, int y, int z) {
                return x;
            }
        },
        Y("y") {
            public int getCoordinate(int x, int y, int z) {
                return y;
            }
        },
        Z("z") {
            public int getCoordinate(int x, int y, int z) {
                return z;
            }
        };

        private static final Map<String, Axis> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(EnumFacing.Axis::getName, (axis) -> axis));
        private final String name;

        Axis(String name) {
            this.name = name;
        }

        public static EnumFacing.Axis byName(String name) {
            return BY_NAME.get(name.toLowerCase(Locale.ROOT));
        }

        @Override
        public String getName() {
            return this.name;
        }

        public boolean isVertical() {
            return this == Y;
        }

        public boolean isHorizontal() {
            return this == X || this == Z;
        }

        public String toString() {
            return this.name;
        }

        public boolean test(EnumFacing facing) {
            return facing != null && facing.getAxis() == this;
        }

        public abstract int getCoordinate(int x, int y, int z);
    }

    public enum AxisDirection {
        POSITIVE,
        NEGATIVE
    }
}
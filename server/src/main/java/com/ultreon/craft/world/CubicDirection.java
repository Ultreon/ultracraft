package com.ultreon.craft.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Axis;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum CubicDirection {
    UP(new Vector3(0, 1, 0), -1, new Quaternion(Vector3.Y, 90 * MathUtils.degRad)),
    DOWN(new Vector3(0, -1, 0), -1, new Quaternion(Vector3.Y, -90 * MathUtils.degRad)),
    NORTH(new Vector3(0, 0, 1), 0, new Quaternion(Vector3.Y, 0 * MathUtils.degRad)),
    WEST(new Vector3(-1, 0, 0), 1, new Quaternion(Vector3.Y, 90 * MathUtils.degRad)),
    SOUTH(new Vector3(0, 0, -1), 2, new Quaternion(Vector3.Y, 180 * MathUtils.degRad)),
    EAST(new Vector3(1, 0, 0), 3, new Quaternion(Vector3.Y, -90 * MathUtils.degRad));

    public static final CubicDirection[] HORIZONTAL = {NORTH, WEST, SOUTH, EAST};

    private final Vector3 normal;
    private final Quaternion rotation;
    public final int hIndex;

    CubicDirection(Vector3 normal, int index, Quaternion rotation) {
        this.normal = normal;
        this.hIndex = index;
        this.rotation = rotation;
    }

    public static @Nullable CubicDirection ofNormal(Vec3f normal) {
        for (CubicDirection face : CubicDirection.values()) {
            if (face.normal.x == normal.x && face.normal.y == normal.y && face.normal.z == normal.z) {
                return face;
            }
        }
        return null;
    }

    public static CubicDirection fromVec3d(Vec3d direction) {
        double[] comps = new double[]{direction.x, direction.y, direction.z};
        double max;

        if (comps[0] > comps[1]) {
            max = Math.max(comps[0], comps[2]);
        } else {
            max = Math.max(comps[1], comps[2]);
        }

        if (max == comps[0]) return max < 0 ? SOUTH : NORTH;
        else if (max == comps[1]) return max < 0 ? DOWN : UP;
        else return max < 0 ? WEST : EAST;
    }

    public Vector3 getNormal() {
        return this.normal;
    }

    public TextObject getDisplayName() {
        return TextObject.translation("ultracraft.block.face." + this.name().toLowerCase(Locale.ROOT));
    }

    public Axis getAxis() {
        return switch (this) {
            case UP, DOWN -> Axis.Y;
            case WEST, EAST -> Axis.X;
            case NORTH, SOUTH -> Axis.Z;
        };
    }

    public CubicDirection getOpposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case WEST -> EAST;
            case EAST -> WEST;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
        };
    }

    public CubicDirection getClockwise() {
        return switch (this) {
            case UP -> UP;
            case DOWN -> DOWN;
            case WEST -> SOUTH;
            case EAST -> NORTH;
            case NORTH -> EAST;
            case SOUTH -> WEST;
        };
    }

    public CubicDirection getCounterClockwise() {
        return switch (this) {
            case UP -> UP;
            case DOWN -> DOWN;
            case WEST -> NORTH;
            case EAST -> SOUTH;
            case NORTH -> WEST;
            case SOUTH -> EAST;
        };
    }

    public Quaternion getHorizontalRotation() {
        return switch (this) {
            case UP -> UP.rotation;
            case DOWN -> DOWN.rotation;
            case WEST -> WEST.rotation;
            case EAST -> EAST.rotation;
            case NORTH -> NORTH.rotation;
            case SOUTH -> SOUTH.rotation;
        };
    }

    public int getIndex() {
        return this.hIndex;
    }

    public CubicDirection rotateY(int hIndex) {
        if (this.hIndex == -1) return this;

        return switch (hIndex) {
            case 0 -> this;
            case 1 -> this.getClockwise();
            case 2 -> this.getClockwise().getClockwise();
            case 3 -> this.getCounterClockwise();
            default -> throw new IllegalArgumentException();
        };
    }
}

package com.ultreon.craft.world;

import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Axis;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum BlockFace {
    UP(new Vector3(0, 1, 0)),
    DOWN(new Vector3(0, -1, 0)),
    WEST(new Vector3(-1, 0, 0)),
    EAST(new Vector3(1, 0, 0)),
    NORTH(new Vector3(0, 0, 1)),
    SOUTH(new Vector3(0, 0, -1));

    private final Vector3 normal;

    BlockFace(Vector3 normal) {
        this.normal = normal;
    }

    public static @Nullable BlockFace ofNormal(Vec3f normal) {
        for (BlockFace face : BlockFace.values()) {
            if (face.normal.x == normal.x && face.normal.y == normal.y && face.normal.z == normal.z) {
                return face;
            }
        }
        return null;
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
}

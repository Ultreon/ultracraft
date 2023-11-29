package com.ultreon.craft.client.world;

import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.util.Axis;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import com.ultreon.libs.translations.v1.Language;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum BlockFace {
    TOP(new Vector3(0, 1, 0), Color.rgb(0xffffff)),
    BOTTOM(new Vector3(0, -1, 0), Color.rgb(0x808080)),
    LEFT(new Vector3(-1, 0, 0), Color.rgb(0xa0a0a0)),
    RIGHT(new Vector3(1, 0, 0), Color.rgb(0xa0a0a0)),
    FRONT(new Vector3(0, 0, 1), Color.rgb(0xd0d0d0)),
    BACK(new Vector3(0, 0, -1), Color.rgb(0xd0d0d0));

    private final Vector3 normal;
    private final Color color;

    BlockFace(Vector3 normal, Color color) {
        this.normal = normal;
        this.color = color;
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

    public void getDisplayName() {
        Language.translate("ultracraft.misc.blockFace." + this.name().toLowerCase(Locale.ROOT));
    }

    public Axis getAxis() {
        return switch (this) {
            case TOP, BOTTOM -> Axis.Y;
            case LEFT, RIGHT -> Axis.X;
            case FRONT, BACK -> Axis.Z;
        };
    }

    public float[] getFaceVertices() {
        return switch (this) {
            case TOP -> ChunkMeshBuilder.topVertices;
            case BOTTOM -> ChunkMeshBuilder.bottomVertices;
            case LEFT -> ChunkMeshBuilder.leftVertices;
            case RIGHT -> ChunkMeshBuilder.rightVertices;
            case FRONT -> ChunkMeshBuilder.frontVertices;
            case BACK -> ChunkMeshBuilder.backVertices;
        };
    }
}

package com.ultreon.craft.client.model.blockbench;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.vector.Vec3f;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class BBModelGroup implements BBModelOutlineInfo, BBModelNode {
    private final BlockBenchModelImporter model;
    private final String name;
    private final Vec3f origin;
    private final Color color;
    private final UUID uuid;
    private final boolean export;
    private final boolean mirrorUV;
    private final boolean isOpen;
    private final boolean visibility;
    private final int autouv;
    private final List<BBModelOutlineInfo> children;
    private final Vec3f rotation;
    BBModelGroup parent;

    public BBModelGroup(BlockBenchModelImporter model, String name, Vec3f origin, Color color, UUID uuid, boolean export, boolean mirrorUV,
                        boolean isOpen, boolean visibility, int autouv, List<BBModelOutlineInfo> children, Vec3f rotation) {
        this.model = model;
        this.name = name;
        this.origin = origin;
        this.color = color;
        this.uuid = uuid;
        this.export = export;
        this.mirrorUV = mirrorUV;
        this.isOpen = isOpen;
        this.visibility = visibility;
        this.autouv = autouv;
        this.children = children;
        this.rotation = rotation;
    }

    private Matrix4 rotationMatrix;

    public BBModelElement element() {
        return model.getElement(this.uuid);
    }

    @Override
    public Matrix4 rotationMatrix() {
        if (this.rotationMatrix == null) {
            this.rotationMatrix = new Matrix4();
            this.rotationMatrix.rotate(Vector3.X, this.rotation.x);
            this.rotationMatrix.rotate(Vector3.Y, this.rotation.y);
            this.rotationMatrix.rotate(Vector3.Z, this.rotation.z);
        }
        return this.rotationMatrix;
    }

    public BlockBenchModelImporter model() {
        return model;
    }

    public String name() {
        return name;
    }

    public Vec3f origin() {
        return origin;
    }

    public Color color() {
        return color;
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

    public boolean export() {
        return export;
    }

    public boolean mirrorUV() {
        return mirrorUV;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean visibility() {
        return visibility;
    }

    public int autouv() {
        return autouv;
    }

    public List<BBModelOutlineInfo> children() {
        return children;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBModelGroup) obj;
        return Objects.equals(this.model, that.model) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.origin, that.origin) &&
                Objects.equals(this.color, that.color) &&
                Objects.equals(this.uuid, that.uuid) &&
                this.export == that.export &&
                this.mirrorUV == that.mirrorUV &&
                this.isOpen == that.isOpen &&
                this.visibility == that.visibility &&
                this.autouv == that.autouv &&
                Objects.equals(this.children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, name, origin, color, uuid, export, mirrorUV, isOpen, visibility, autouv, children);
    }

    @Override
    public Vec3f rotation() {
        return rotation;
    }

    @Override
    public BBModelNode parent() {
        return parent;
    }

    @Override
    public String toString() {
        return "BBModelGroup[" +
                "model=" + model + ", " +
                "name=" + name + ", " +
                "origin=" + origin + ", " +
                "color=" + color + ", " +
                "uuid=" + uuid + ", " +
                "export=" + export + ", " +
                "mirrorUV=" + mirrorUV + ", " +
                "isOpen=" + isOpen + ", " +
                "visibility=" + visibility + ", " +
                "autouv=" + autouv + ", " +
                "children=" + children + ", " +
                "rotation=" + rotation + ']';
    }
}

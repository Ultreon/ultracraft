package com.ultreon.craft.client.model.blockbench;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.ultreon.craft.util.Color;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec3f;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class BBMeshModelElement extends BBModelElement {
    private final String name;
    private final Color color;
    private final Vec3f origin;
    private final Vec3f rotation;
    private final boolean export;
    private final boolean visibility;
    private final boolean locked;
    private final String renderOrder;
    private final boolean allowMirrorModeling;
    private final List<BBModelMeshFace> faces;

    public BBMeshModelElement(String name, Color color, Vec3f origin, Vec3f rotation, boolean export,
                              boolean visibility, boolean locked, String renderOrder, boolean allowMirrorModeling,
                              List<BBModelMeshFace> faces) {
        this.name = name;
        this.color = color;
        this.origin = origin;
        this.rotation = rotation;
        this.export = export;
        this.visibility = visibility;
        this.locked = locked;
        this.renderOrder = renderOrder;
        this.allowMirrorModeling = allowMirrorModeling;
        this.faces = faces;
    }

    public String name() {
        return name;
    }

    public Color color() {
        return color;
    }

    public Vec3f origin() {
        return origin;
    }

    public Vec3f rotation() {
        return rotation;
    }

    public boolean export() {
        return export;
    }

    public boolean visibility() {
        return visibility;
    }

    public boolean locked() {
        return locked;
    }

    public String renderOrder() {
        return renderOrder;
    }

    public boolean allowMirrorModeling() {
        return allowMirrorModeling;
    }

    public List<BBModelMeshFace> faces() {
        return faces;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBMeshModelElement) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.color, that.color) &&
                Objects.equals(this.origin, that.origin) &&
                Objects.equals(this.rotation, that.rotation) &&
                this.export == that.export &&
                this.visibility == that.visibility &&
                this.locked == that.locked &&
                Objects.equals(this.renderOrder, that.renderOrder) &&
                this.allowMirrorModeling == that.allowMirrorModeling &&
                Objects.equals(this.faces, that.faces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, color, origin, rotation, export, visibility, locked, renderOrder, allowMirrorModeling, faces);
    }

    @Override
    public String toString() {
        return "BBMeshModelElement[" +
                "name=" + name + ", " +
                "color=" + color + ", " +
                "origin=" + origin + ", " +
                "rotation=" + rotation + ", " +
                "export=" + export + ", " +
                "visibility=" + visibility + ", " +
                "locked=" + locked + ", " +
                "renderOrder=" + renderOrder + ", " +
                "allowMirrorModeling=" + allowMirrorModeling + ", " +
                "faces=" + faces + ']';
    }

    @Override
    public void write(ModelBuilder builder, Map<Integer, MeshBuilder> texture2builder, BlockBenchModelImporter modelData, Vec2f resolution) {
        for (BBModelMeshFace face : faces) {
            face.write(builder, texture2builder, resolution);
        }
    }
}

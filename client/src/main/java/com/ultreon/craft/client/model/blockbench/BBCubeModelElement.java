package com.ultreon.craft.client.model.blockbench;

import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.ultreon.craft.util.Color;
import com.ultreon.craft.world.CubicDirection;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec3f;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class BBCubeModelElement extends BBModelElement {
    private final String name;
    private final boolean boxUv;
    private final boolean rescale;
    private final boolean locked;
    private final String renderOrder;
    private final boolean allowMirrorModeling;
    private final Vec3f from;
    private final Vec3f to;
    private final float autouv;
    private final Color color;
    private final Vec3f origin;
    private final List<BBModelFace> faces;
    private final UUID uuid;

    public BBCubeModelElement(String name, boolean boxUv, boolean rescale, boolean locked, String renderOrder,
                              boolean allowMirrorModeling, Vec3f from, Vec3f to, float autouv, Color color,
                              Vec3f origin, List<BBModelFace> faces, UUID uuid) {
        this.name = name;
        this.boxUv = boxUv;
        this.rescale = rescale;
        this.locked = locked;
        this.renderOrder = renderOrder;
        this.allowMirrorModeling = allowMirrorModeling;
        this.from = from;
        this.to = to;
        this.autouv = autouv;
        this.color = color;
        this.origin = origin;
        this.faces = faces;
        this.uuid = uuid;
    }

    public String name() {
        return name;
    }

    public boolean boxUv() {
        return boxUv;
    }

    public boolean rescale() {
        return rescale;
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

    public Vec3f from() {
        return from;
    }

    public Vec3f to() {
        return to;
    }

    public float autouv() {
        return autouv;
    }

    public Color color() {
        return color;
    }

    public Vec3f origin() {
        return origin;
    }

    public List<BBModelFace> faces() {
        return faces;
    }

    public UUID uuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBCubeModelElement) obj;
        return Objects.equals(this.name, that.name) &&
                this.boxUv == that.boxUv &&
                this.rescale == that.rescale &&
                this.locked == that.locked &&
                Objects.equals(this.renderOrder, that.renderOrder) &&
                this.allowMirrorModeling == that.allowMirrorModeling &&
                Objects.equals(this.from, that.from) &&
                Objects.equals(this.to, that.to) &&
                Float.floatToIntBits(this.autouv) == Float.floatToIntBits(that.autouv) &&
                Objects.equals(this.color, that.color) &&
                Objects.equals(this.origin, that.origin) &&
                Objects.equals(this.faces, that.faces) &&
                Objects.equals(this.uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, boxUv, rescale, locked, renderOrder, allowMirrorModeling, from, to, autouv, color, origin, faces, uuid);
    }

    @Override
    public String toString() {
        return "BBModelElement[" +
                "name=" + name + ", " +
                "boxUv=" + boxUv + ", " +
                "rescale=" + rescale + ", " +
                "locked=" + locked + ", " +
                "renderOrder=" + renderOrder + ", " +
                "allowMirrorModeling=" + allowMirrorModeling + ", " +
                "from=" + from + ", " +
                "vec3f=" + to + ", " +
                "autouv=" + autouv + ", " +
                "color=" + color + ", " +
                "origin=" + origin + ", " +
                "faces=" + faces + ", " +
                "uuid=" + uuid + ']';
    }

    @Override
    public void write(ModelBuilder model, Map<Integer, MeshBuilder> texture2builder, BlockBenchModelImporter modelData, Vec2f resolution) {
        this.bake(texture2builder, resolution);
    }


    public void bake(Map<Integer, MeshBuilder> texture2builder, Vec2f resolution) {
        Vec3f from = this.from().cpy().div(16);
        Vec3f to = this.to().cpy().div(16);

        MeshPartBuilder.VertexInfo v00 = new MeshPartBuilder.VertexInfo();
        MeshPartBuilder.VertexInfo v01 = new MeshPartBuilder.VertexInfo();
        MeshPartBuilder.VertexInfo v10 = new MeshPartBuilder.VertexInfo();
        MeshPartBuilder.VertexInfo v11 = new MeshPartBuilder.VertexInfo();
        for (BBModelFace entry : faces) {
            CubicDirection blockFace = entry.blockFace();
            int texRef = entry.texture();
            MeshBuilder meshBuilder = texture2builder.get(texRef);

            v00.setCol(Color.MAGENTA.toGdx());
            v01.setCol(Color.MAGENTA.toGdx());
            v10.setCol(Color.MAGENTA.toGdx());
            v11.setCol(Color.MAGENTA.toGdx());

            v00.setNor(blockFace.getNormal());
            v01.setNor(blockFace.getNormal());
            v10.setNor(blockFace.getNormal());
            v11.setNor(blockFace.getNormal());

            v00.setUV(entry.uv().x / resolution.x, entry.uv().w / resolution.y);
            v01.setUV(entry.uv().x / resolution.x, entry.uv().y / resolution.y);
            v10.setUV(entry.uv().z / resolution.x, entry.uv().w / resolution.y);
            v11.setUV(entry.uv().z / resolution.x, entry.uv().y / resolution.y);

            v00.setUV(0, 1);
            v01.setUV(0, 0);
            v10.setUV(1, 1);
            v11.setUV(1, 0);

            switch (blockFace) {
                case UP -> {
                    v00.setPos(to.x, to.y, from.z);
                    v01.setPos(to.x, to.y, to.z);
                    v10.setPos(from.x, to.y, from.z);
                    v11.setPos(from.x, to.y, to.z);
                }
                case DOWN -> {
                    v00.setPos(from.x, from.y, from.z);
                    v01.setPos(from.x, from.y, to.z);
                    v10.setPos(to.x, from.y, from.z);
                    v11.setPos(to.x, from.y, to.z);
                }
                case WEST -> {
                    v00.setPos(from.x, from.y, from.z);
                    v01.setPos(from.x, to.y, from.z);
                    v10.setPos(from.x, from.y, to.z);
                    v11.setPos(from.x, to.y, to.z);
                }
                case EAST -> {
                    v00.setPos(to.x, from.y, to.z);
                    v01.setPos(to.x, to.y, to.z);
                    v10.setPos(to.x, from.y, from.z);
                    v11.setPos(to.x, to.y, from.z);
                }
                case NORTH -> {
                    v00.setPos(to.x, from.y, from.z);
                    v01.setPos(to.x, to.y, from.z);
                    v10.setPos(from.x, from.y, from.z);
                    v11.setPos(from.x, to.y, from.z);
                }
                case SOUTH -> {
                    v00.setPos(from.x, from.y, to.z);
                    v01.setPos(from.x, to.y, to.z);
                    v10.setPos(to.x, from.y, to.z);
                    v11.setPos(to.x, to.y, to.z);
                }
            }

            meshBuilder.rect(v00, v10, v11, v01);
        }
    }
}

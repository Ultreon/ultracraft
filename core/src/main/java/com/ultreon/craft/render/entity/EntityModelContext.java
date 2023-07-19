package com.ultreon.craft.render.entity;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FlushablePool;
import com.ultreon.craft.util.BoundingBox;
import com.ultreon.libs.commons.v0.size.IntSize;
import com.ultreon.libs.commons.v0.vector.Vec3d;

import java.util.HashMap;
import java.util.Map;

public class EntityModelContext {
    Map<String, Mesh> meshes = new HashMap<>();
    Map<String, Box> boxes = new HashMap<>();
    Map<String, Quaternion> rotations = new HashMap<>();
    Map<String, Vector3> offsets = new HashMap<>();

    private final static FlushablePool<Vector3> vectorPool = new FlushablePool<Vector3>() {
        @Override
        protected Vector3 newObject () {
            return new Vector3();
        }
    };

    /* Vector3 */
    private static final Vector3 tmpV1 = new Vector3();
    private static final Vector3 tmpV2 = new Vector3();

    /* VertexInfo */
    private static final MeshPartBuilder.VertexInfo vertTmp0 = new MeshPartBuilder.VertexInfo();
    private static final MeshPartBuilder.VertexInfo vertTmp1 = new MeshPartBuilder.VertexInfo();
    private static final MeshPartBuilder.VertexInfo vertTmp2 = new MeshPartBuilder.VertexInfo();
    private static final MeshPartBuilder.VertexInfo vertTmp3 = new MeshPartBuilder.VertexInfo();
    private static final MeshPartBuilder.VertexInfo vertTmp4 = new MeshPartBuilder.VertexInfo();
    private static final MeshPartBuilder.VertexInfo vertTmp5 = new MeshPartBuilder.VertexInfo();
    private static final MeshPartBuilder.VertexInfo vertTmp6 = new MeshPartBuilder.VertexInfo();
    private static final MeshPartBuilder.VertexInfo vertTmp7 = new MeshPartBuilder.VertexInfo();
    private static final MeshPartBuilder.VertexInfo vertTmp8 = new MeshPartBuilder.VertexInfo();

    private final IntSize textureSize;

    public EntityModelContext(IntSize textureSize) {
        this.textureSize = textureSize;
    }

    public IntSize textureSize() {
        return this.textureSize;
    }

    public EntityModelContext box(String name, float x, float y, float z, float width, float height, float depth) {
        this.boxes.put(name, new Box(this, new BoundingBox(new Vec3d(x, y, z), new Vec3d(x + width, y + height, z + depth))));
        return this;
    }

    public void build() {
        this.boxes.forEach((name, box) -> {
            MeshBuilder meshBuilder = new MeshBuilder();
            meshBuilder.begin(new VertexAttributes(VertexAttribute.ColorPacked(), VertexAttribute.Position(), VertexAttribute.TexCoords(0)));
            build(meshBuilder, (float) box.box.min.x, (float) box.box.min.y, (float) box.box.min.z, (float) (box.box.max.x - box.box.min.x), (float) (box.box.max.y - box.box.min.y), (float) (box.box.max.z - box.box.min.z));
            Mesh mesh = meshBuilder.end();
            mesh.transform(new Matrix4(box.rotation));
            this.meshes.put(name, mesh);
        });
    }

    public void setRotation(String name, Quaternion quaternion) {
        this.rotations.put(name, quaternion);
    }

    public Quaternion getRotation(String name) {
        return this.rotations.getOrDefault(name, new Quaternion());
    }

    public void setOffset(String name, Vector3 quaternion) {
        this.offsets.put(name, quaternion);
    }

    public Vector3 getOffset(String name) {
        return this.offsets.getOrDefault(name, new Vector3());
    }

    /** Add a box. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
    private static void build (MeshPartBuilder builder, MeshPartBuilder.VertexInfo corner000, MeshPartBuilder.VertexInfo corner010, MeshPartBuilder.VertexInfo corner100,
                              MeshPartBuilder.VertexInfo corner110, MeshPartBuilder.VertexInfo corner001, MeshPartBuilder.VertexInfo corner011, MeshPartBuilder.VertexInfo corner101, MeshPartBuilder.VertexInfo corner111) {
        builder.ensureVertices(8);
        final short i000 = builder.vertex(corner000);
        final short i100 = builder.vertex(corner100);
        final short i110 = builder.vertex(corner110);
        final short i010 = builder.vertex(corner010);
        final short i001 = builder.vertex(corner001);
        final short i101 = builder.vertex(corner101);
        final short i111 = builder.vertex(corner111);
        final short i011 = builder.vertex(corner011);

        final int primitiveType = builder.getPrimitiveType();
        if (primitiveType == GL20.GL_LINES) {
            builder.ensureIndices(24);
            builder.rect(i000, i100, i110, i010);
            builder.rect(i101, i001, i011, i111);
            builder.index(i000, i001, i010, i011, i110, i111, i100, i101);
        } else if (primitiveType == GL20.GL_POINTS) {
            builder.ensureRectangleIndices(2);
            builder.rect(i000, i100, i110, i010);
            builder.rect(i101, i001, i011, i111);
        } else { // GL20.GL_TRIANGLES
            builder.ensureRectangleIndices(6);
            builder.rect(i000, i100, i110, i010);
            builder.rect(i101, i001, i011, i111);
            builder.rect(i000, i010, i011, i001);
            builder.rect(i101, i111, i110, i100);
            builder.rect(i101, i100, i000, i001);
            builder.rect(i110, i111, i011, i010);
        }
    }

    /** Add a box. Requires GL_POINTS, GL_LINES or GL_TRIANGLES primitive type. */
    private static void build(MeshPartBuilder builder, Vector3 corner000, Vector3 corner010, Vector3 corner100, Vector3 corner110,
                              Vector3 corner001, Vector3 corner011, Vector3 corner101, Vector3 corner111) {
        if ((builder.getAttributes().getMask() & (VertexAttributes.Usage.Normal | VertexAttributes.Usage.BiNormal | VertexAttributes.Usage.Tangent | VertexAttributes.Usage.TextureCoordinates)) == 0) {
            build(builder, vertTmp1.set(corner000, null, null, null), vertTmp2.set(corner010, null, null, null),
                    vertTmp3.set(corner100, null, null, null), vertTmp4.set(corner110, null, null, null),
                    vertTmp5.set(corner001, null, null, null), vertTmp6.set(corner011, null, null, null),
                    vertTmp7.set(corner101, null, null, null), vertTmp8.set(corner111, null, null, null));
        } else {
            builder.ensureVertices(24);
            builder.ensureRectangleIndices(6);
            Vector3 nor = tmpV1.set(corner000).lerp(corner110, 0.5f).sub(tmpV2.set(corner001).lerp(corner111, 0.5f)).nor();
            builder.rect(corner000, corner010, corner110, corner100, nor);
            builder.rect(corner011, corner001, corner101, corner111, nor.scl(-1));
            nor = tmpV1.set(corner000).lerp(corner101, 0.5f).sub(tmpV2.set(corner010).lerp(corner111, 0.5f)).nor();
            builder.rect(corner001, corner000, corner100, corner101, nor);
            builder.rect(corner010, corner011, corner111, corner110, nor.scl(-1));
            nor = tmpV1.set(corner000).lerp(corner011, 0.5f).sub(tmpV2.set(corner100).lerp(corner111, 0.5f)).nor();
            builder.rect(corner001, corner011, corner010, corner000, nor);
            builder.rect(corner100, corner110, corner111, corner101, nor.scl(-1));
        }
    }

    /** Add a box at the specified location, with the specified dimensions */
    public static void build (MeshPartBuilder builder, float x, float y, float z, float width, float height, float depth) {
        final float hw = width * 0.5f;
        final float hh = height * 0.5f;
        final float hd = depth * 0.5f;
        final float x0 = x - hw, y0 = y - hh, z0 = z - hd, x1 = x + hw, y1 = y + hh, z1 = z + hd;
        build(builder, //
                obtainV3().set(x0, y0, z0), obtainV3().set(x0, y1, z0), obtainV3().set(x1, y0, z0), obtainV3().set(x1, y1, z0), //
                obtainV3().set(x0, y0, z1), obtainV3().set(x0, y1, z1), obtainV3().set(x1, y0, z1), obtainV3().set(x1, y1, z1));
        freeAll();
    }

    /** Obtain a temporary {@link Vector3} object, must be free'd using {@link #freeAll()}. */
    private static Vector3 obtainV3() {
        return vectorPool.obtain();
    }

    /** Free all objects obtained using one of the `obtainXX` methods. */
    private static void freeAll() {
        vectorPool.flush();
    }
}

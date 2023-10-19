package com.ultreon.craft.render.model;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;

import java.util.Objects;

import static com.ultreon.craft.world.Chunk.VERTEX_SIZE;

public final class BakedCubeModel implements Disposable {
    private final TextureRegion top;
    private final TextureRegion bottom;
    private final TextureRegion left;
    private final TextureRegion right;
    private final TextureRegion front;
    private final TextureRegion back;
    private final Mesh mesh;

    public BakedCubeModel(TextureRegion all) {
        this(all, all, all, all, all, all);
    }

    public BakedCubeModel(TextureRegion top, TextureRegion bottom,
                          TextureRegion left, TextureRegion right,
                          TextureRegion front, TextureRegion back) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.front = front;
        this.back = back;
        this.mesh = this.createMesh();
        this.mesh.transform(new Matrix4().setToTranslation(-1F, 0, 0F));

        UltreonCraft.get().deferDispose(this);
    }

    public TextureRegion top() {
        return this.top;
    }

    public TextureRegion bottom() {
        return this.bottom;
    }

    public TextureRegion left() {
        return this.left;
    }

    public TextureRegion right() {
        return this.right;
    }

    public TextureRegion front() {
        return this.front;
    }

    public TextureRegion back() {
        return this.back;
    }
    
    @SuppressWarnings("UnusedAssignment")
    private Mesh createMesh() {
        int len = World.CHUNK_SIZE * World.CHUNK_SIZE * World.CHUNK_SIZE * 6 * 6 / 3;

        short[] indices = new short[len];
        short j = 0;
        for (int i = 0; i < len; i += 6, j += 4) {
            indices[i] = j;
            indices[i + 1] = (short) (j + 1);
            indices[i + 2] = (short) (j + 2);
            indices[i + 3] = (short) (j + 2);
            indices[i + 4] = (short) (j + 3);
            indices[i + 5] = j;
        }

        FloatList vertices = new FloatArrayList();
        int i = 0;
        Vec3i offset = new Vec3i();

        BakedCubeModel model = this;

        BakedCubeModel.createTop(offset, 0, 0, 0, model.top(), vertices);
        BakedCubeModel.createBottom(offset, 0, 0, 0, model.bottom(), vertices);
        BakedCubeModel.createLeft(offset, 0, 0, 0, model.left(), vertices);
        BakedCubeModel.createRight(offset, 0, 0, 0, model.right(), vertices);
        BakedCubeModel.createFront(offset, 0, 0, 0, model.front(), vertices);
        BakedCubeModel.createBack(offset, 0, 0, 0, model.back(), vertices);

        int numVertices = vertices.size() / VERTEX_SIZE + 1;
        Mesh mesh = new Mesh(false, false, numVertices, indices.length * 6, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)));
        mesh.setIndices(indices);
        numVertices = numVertices / 4 * 6;
        mesh.setVertices(vertices.toFloatArray());
        vertices.clear();
        indices = null;
        return mesh;
    }

    private static void createTop(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + x);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + x);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());
    }

    private static void createBottom(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + x);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + x);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());
    }

    private static void createLeft(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + x);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + x);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());

        vertices.add(offset.x + x);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z + 1);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + x);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z + 1);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());
    }

    private static void createRight(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z + 1);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z + 1);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());
    }

    private static void createFront(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + x);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + x);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU2());
        vertices.add(region.getV());
    }

    private static void createBack(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + x);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + x);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU2());
        vertices.add(region.getV());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y + 1);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + x + 1);
        vertices.add(offset.y + y);
        vertices.add(offset.z + z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU());
        vertices.add(region.getV2());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BakedCubeModel that = (BakedCubeModel) obj;
        return Objects.equals(this.top, that.top) &&
                Objects.equals(this.bottom, that.bottom) &&
                Objects.equals(this.left, that.left) &&
                Objects.equals(this.right, that.right) &&
                Objects.equals(this.front, that.front) &&
                Objects.equals(this.back, that.back);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.top, this.bottom, this.left, this.right, this.front, this.back);
    }

    @Override
    public String toString() {
        return "BakedCubeModel[" +
                "top=" + this.top + ", " +
                "bottom=" + this.bottom + ", " +
                "left=" + this.left + ", " +
                "right=" + this.right + ", " +
                "front=" + this.front + ", " +
                "back=" + this.back + ']';
    }

    public Mesh getMesh() {
        return this.mesh;
    }

    public void dispose() {
        this.mesh.dispose();
    }
}

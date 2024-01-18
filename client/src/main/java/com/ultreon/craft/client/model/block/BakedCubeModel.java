package com.ultreon.craft.client.model.block;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.texture.TextureManager;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;

import java.util.Objects;

import static com.ultreon.craft.world.Chunk.VERTEX_SIZE;

public final class BakedCubeModel implements BlockModel {
    public static final BakedCubeModel DEFAULT = new BakedCubeModel(new ElementID("block/default"), TextureManager.DEFAULT_TEX_REG);
    private final ElementID resourceId;
    private final TextureRegion top;
    private final TextureRegion bottom;
    private final TextureRegion left;
    private final TextureRegion right;
    private final TextureRegion front;
    private final TextureRegion back;
    private final Mesh mesh;
    public final ModelProperties properties;
    private final Model model;

    public BakedCubeModel(ElementID resourceId, TextureRegion all) {
        this(resourceId, all, all, all, all, all, all);
    }

    public BakedCubeModel(ElementID resourceId, TextureRegion top, TextureRegion bottom,
                          TextureRegion left, TextureRegion right,
                          TextureRegion front, TextureRegion back) {
        this(resourceId, top, bottom, left, right, front, back, ModelProperties.builder().build());
    }

    public BakedCubeModel(ElementID resourceId, TextureRegion all, ModelProperties properties) {
        this(resourceId, all, all, all, all, all, all, properties);
    }

    public BakedCubeModel(ElementID resourceId, TextureRegion top, TextureRegion bottom,
                          TextureRegion left, TextureRegion right,
                          TextureRegion front, TextureRegion back, ModelProperties properties) {
        this.resourceId = resourceId;
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.front = front;
        this.back = back;

        mesh = this.createMesh();
        mesh.transform(new Matrix4().setToTranslation(-1F, 0, 0F));
        this.properties = properties;

        UltracraftClient client = UltracraftClient.get();

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        Material material = new Material();
        material.set(new TextureAttribute(TextureAttribute.Diffuse, client.blocksTextureAtlas.getTexture()));
        material.set(new TextureAttribute(TextureAttribute.Emissive, client.blocksTextureAtlas.getEmissiveTexture()));

        modelBuilder.part("cube", this.mesh, GL20.GL_TRIANGLES, material);
        this.model = modelBuilder.end();
    }

    public TextureRegion top() {
        return this.top;
    }

    public TextureRegion bottom() {
        return this.bottom;
    }

    public TextureRegion west() {
        return this.left;
    }

    public TextureRegion east() {
        return this.right;
    }

    public TextureRegion north() {
        return this.front;
    }

    public TextureRegion south() {
        return this.back;
    }

    private Mesh createMesh() {
        int len = World.CHUNK_SIZE * World.CHUNK_SIZE * World.CHUNK_SIZE * 6 * 6 / 3;

        short[] indices = new short[len];
        short j = 0;
        for (int i = 0; i < len; i += 6, j += 4) {
            indices[i] = j;
            indices[i + 1] = (short) (j + 1f);
            indices[i + 2] = (short) (j + 2);
            indices[i + 3] = (short) (j + 2);
            indices[i + 4] = (short) (j + 3);
            indices[i + 5] = j;
        }

        FloatList vertices = new FloatArrayList();
        Vec3i offset = new Vec3i();

        BakedCubeModel model = this;

        BakedCubeModel.createTop(offset, 0, 0, 0, model.top(), vertices);
        BakedCubeModel.createBottom(offset, 0, 0, 0, model.bottom(), vertices);
        BakedCubeModel.createLeft(offset, 0, 0, 0, model.west(), vertices);
        BakedCubeModel.createRight(offset, 0, 0, 0, model.east(), vertices);
        BakedCubeModel.createFront(offset, 0, 0, 0, model.north(), vertices);
        BakedCubeModel.createBack(offset, 0, 0, 0, model.south(), vertices);

        int numVertices = vertices.size() / VERTEX_SIZE + 1;
        Mesh mesh = new Mesh(false, false, numVertices, indices.length * 6, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)));
        mesh.setIndices(indices);
        mesh.setVertices(vertices.toFloatArray());
        vertices.clear();
        return mesh;
    }

    private static void createTop(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + (float) x);
        vertices.add(offset.y + y + 1f);
        vertices.add(offset.z + (float) z);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + x + 1f);
        vertices.add(offset.y + y + 1f);
        vertices.add(offset.z + (float) z);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());

        vertices.add(offset.x + x + 1f);
        vertices.add(offset.y + y + 1f);
        vertices.add(offset.z + z + 1f);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + (float) x);
        vertices.add(offset.y + y + 1f);
        vertices.add(offset.z + z + 1f);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());
    }

    private static void createBottom(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + (float) x);
        vertices.add(offset.y + (float) y);
        vertices.add(offset.z + (float) z);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + (float) x);
        vertices.add(offset.y + (float) y);
        vertices.add(offset.z + z + 1f);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1f);
        vertices.add(offset.y + (float) y);
        vertices.add(offset.z + z + 1f);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1f);
        vertices.add(offset.y + (float) y);
        vertices.add(offset.z + (float) z);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());
    }

    private static void createLeft(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + (float) x);
        vertices.add(offset.y + (float) y);
        vertices.add(offset.z + (float) z);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + (float) x);
        vertices.add(offset.y + y + 1f);
        vertices.add(offset.z + (float) z);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());

        vertices.add(offset.x + (float) x);
        vertices.add(offset.y + y + 1f);
        vertices.add(offset.z + z + 1f);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + (float) x);
        vertices.add(offset.y + (float) y);
        vertices.add(offset.z + z + 1f);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());
    }

    private static void createRight(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + x + 1f);
        vertices.add(offset.y + (float) y);
        vertices.add(offset.z + (float) z);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1f);
        vertices.add(offset.y + (float) y);
        vertices.add(offset.z + z + 1f);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1f);
        vertices.add(offset.y + y + 1f);
        vertices.add(offset.z + z + 1f);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + x + 1f);
        vertices.add(offset.y + y + 1f);
        vertices.add(offset.z + (float) z);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());
    }

    private static void createFront(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + (float) x);
        vertices.add(offset.y + (float) y);
        vertices.add(offset.z + (float) z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1f);
        vertices.add(offset.y + (float) y);
        vertices.add(offset.z + (float) z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU());
        vertices.add(region.getV2());

        vertices.add(offset.x + x + 1f);
        vertices.add(offset.y + y + 1f);
        vertices.add(offset.z + (float) z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + (float) x);
        vertices.add(offset.y + y + 1f);
        vertices.add(offset.z + (float) z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU2());
        vertices.add(region.getV());
    }

    private static void createBack(Vec3i offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
        vertices.add(offset.x + (float) x);
        vertices.add(offset.y + (float) y);
        vertices.add(offset.z + z + 1f);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(offset.x + (float) x);
        vertices.add(offset.y + y + 1f);
        vertices.add(offset.z + z + 1f);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU2());
        vertices.add(region.getV());

        vertices.add(offset.x + x + 1f);
        vertices.add(offset.y + y + 1f);
        vertices.add(offset.z + z + 1f);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(offset.x + x + 1f);
        vertices.add(offset.y + (float) y);
        vertices.add(offset.z + z + 1f);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU());
        vertices.add(region.getV2());
    }

    @Override
    public void load(UltracraftClient client) {
        // Do nothing
    }

    @Override
    public ElementID resourceId() {
        return resourceId;
    }

    public boolean isCustom() {
        return false;
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

    @Override
    public Model getModel() {
        return this.model;
    }

    @Override
    public void dispose() {
        this.model.dispose();
    }
}

package com.ultreon.craft.client.model.block;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.texture.TextureManager;
import com.ultreon.craft.util.Identifier;

import java.util.Objects;

public final class BakedCubeModel implements BlockModel {
    public static final BakedCubeModel DEFAULT = new BakedCubeModel(new Identifier("block/default"), TextureManager.DEFAULT_TEX_REG);
    private final Identifier resourceId;
    private final TextureRegion top;
    private final TextureRegion bottom;
    private final TextureRegion left;
    private final TextureRegion right;
    private final TextureRegion front;
    private final TextureRegion back;
    private final Mesh mesh;
    public final ModelProperties properties;
    private final Model model;

    private final VertexInfo v00 = new VertexInfo();
    private final VertexInfo v01 = new VertexInfo();
    private final VertexInfo v10 = new VertexInfo();
    private final VertexInfo v11 = new VertexInfo();

    public BakedCubeModel(Identifier resourceId, TextureRegion all) {
        this(resourceId, all, all, all, all, all, all);
    }

    public BakedCubeModel(Identifier resourceId, TextureRegion top, TextureRegion bottom,
                          TextureRegion left, TextureRegion right,
                          TextureRegion front, TextureRegion back) {
        this(resourceId, top, bottom, left, right, front, back, ModelProperties.builder().build());
    }

    public BakedCubeModel(Identifier resourceId, TextureRegion all, ModelProperties properties) {
        this(resourceId, all, all, all, all, all, all, properties);
    }

    public BakedCubeModel(Identifier resourceId, TextureRegion top, TextureRegion bottom,
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
        BakedCubeModel model = this;

        MeshBuilder builder = new MeshBuilder();
        builder.begin(new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)), GL20.GL_TRIANGLES);
        
        this.createTop(-1, 0, 0, model.top(), builder);
        this.createBottom(-1, 0, 0, model.bottom(), builder);
        this.createLeft(-1, 0, 0, model.west(), builder);
        this.createRight(-1, 0, 0, model.east(), builder);
        this.createFront(-1, 0, 0, model.north(), builder);
        this.createBack(-1, 0, 0, model.south(), builder);

        return builder.end();
    }

    private void createTop(int x, int y, int z, TextureRegion region, MeshBuilder builder) {
        if (region == null) return;

        this.v00.setPos(x, y + 1, z);
        this.v01.setPos(x + 1, y + 1, z);
        this.v10.setPos(x + 1, y + 1, z + 1);
        this.v11.setPos(x, y + 1, z + 1);

        this.setNor(0, 1, 0);
        this.finishRect(region, builder);
    }

    private void createBottom(int x, int y, int z, TextureRegion region, MeshBuilder builder) {
        if (region == null) return;

        this.v00.setPos(x, y, z);
        this.v01.setPos(x, y, z + 1);
        this.v10.setPos(x + 1, y, z + 1);
        this.v11.setPos(x + 1, y, z);

        this.setNor(0, -1, 0);
        this.finishRect(region, builder);
    }

    private void createLeft(int x, int y, int z, TextureRegion region, MeshBuilder builder) {
        if (region == null) return;

        this.v00.setPos(x, y, z);
        this.v01.setPos(x, y + 1, z);
        this.v10.setPos(x, y + 1, z + 1);
        this.v11.setPos(x, y, z + 1);

        this.setNor(-1, 0, 0);
        this.finishRect(region, builder);
    }

    private void createRight(int x, int y, int z, TextureRegion region, MeshBuilder builder) {
        if (region == null) return;

        this.v00.setPos(x + 1, y, z);
        this.v01.setPos(x + 1, y, z + 1);
        this.v10.setPos(x + 1, y + 1, z + 1);
        this.v11.setPos(x + 1, y + 1, z);

        this.setNor(1, 0, 0);
        this.finishRect(region, builder);
    }

    private void createFront(int x, int y, int z, TextureRegion region, MeshBuilder builder) {
        if (region == null) return;

        this.v00.setPos(x, y, z);
        this.v01.setPos(x + 1, y, z);
        this.v10.setPos(x + 1, y + 1, z);
        this.v11.setPos(x, y + 1, z);

        this.setNor(0, 0, 1);
        this.finishRect(region, builder);
    }

    private void createBack(int x, int y, int z, TextureRegion region, MeshBuilder builder) {
        if (region == null) return;

        this.v00.setPos(x, y, z + 1);
        this.v01.setPos(x, y + 1, z + 1);
        this.v10.setPos(x + 1, y + 1, z + 1);
        this.v11.setPos(x + 1, y, z + 1);

        this.setNor(0, 0, -1);
        this.finishRect(region, builder);
    }

    private void setNor(int x, int y, int z) {
        this.v00.setNor(x, y, z);
        this.v01.setNor(x, y, z);
        this.v10.setNor(x, y, z);
        this.v11.setNor(x, y, z);
    }

    private void finishRect(TextureRegion region, MeshBuilder builder) {
        this.v00.setUV(region.getU2(), region.getV2());
        this.v01.setUV(region.getU2(), region.getV());
        this.v10.setUV(region.getU(), region.getV());
        this.v11.setUV(region.getU(), region.getV2());

        builder.rect(this.v00, this.v01, this.v10, this.v11);
    }

    @Override
    public void load(UltracraftClient client) {
        // Do nothing
    }

    @Override
    public Identifier resourceId() {
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

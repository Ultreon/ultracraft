package com.ultreon.craft.client.model.entity;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.world.BlockFace;
import com.ultreon.craft.util.MathHelper;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.checkerframework.common.returnsreceiver.qual.This;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;

public class BoxBuilder {
    static float[] backUv = {1, 1, 1, 0, 0, 0, 0, 1,};
    static float[] frontUv = {1, 1, 0, 1, 0, 0, 1, 0,};
    static float[] rightUv = {1, 1, 0, 1, 0, 0, 1, 0,};
    static float[] leftUv = {1, 1, 1, 0, 0, 0, 0, 1,};
    static float[] bottomUv = {0, 0, 0, 1, 1, 1, 0, 1,};
    static float[] topUv = {0, 0, 1, 0, 1, 1, 0, 1};
    static float[] topVertices = {0, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1};
    static float[] bottomVertices = {0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0};
    static float[] leftVertices = {0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1};
    static float[] rightVertices = {1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0};
    static float[] frontVertices = {0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0,};
    static float[] backVertices = {0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1,};

    private final float x;
    private final float y;
    private final float z;
    private final float width;
    private final float height;
    private final float depth;
    private int u;
    private int v;

    public BoxBuilder(MeshBuilder builder, int x, int y, int z, int width, int height, int depth) {

        this.x = x / 16f;
        this.y = y / 16f;
        this.z = z / 16f;
        this.width = width / 16f;
        this.height = height / 16f;
        this.depth = depth / 16f;
    }

    public @This BoxBuilder uv(int u, int v) {
        this.u = u;
        this.v = v;
        return this;
    }

    private void face(Vec3i pos, BlockFace face, TextureRegion region, FloatArray output) {
        float[] vertices = switch (face) {
            case TOP -> BoxBuilder.topVertices;
            case BOTTOM -> BoxBuilder.bottomVertices;
            case LEFT -> BoxBuilder.leftVertices;
            case RIGHT -> BoxBuilder.rightVertices;
            case FRONT -> BoxBuilder.frontVertices;
            case BACK -> BoxBuilder.backVertices;
        };

        float[] uvs = switch (face) {
            case TOP -> BoxBuilder.topUv;
            case BOTTOM -> BoxBuilder.bottomUv;
            case LEFT -> BoxBuilder.leftUv;
            case RIGHT -> BoxBuilder.rightUv;
            case FRONT -> BoxBuilder.frontUv;
            case BACK -> BoxBuilder.backUv;
        };

        Vector3 normal = face.getNormal();

        // Loop vertices and uvs and add them to the output.
        for (int vertexIdx = 0, uvIdx = 0; vertexIdx < vertices.length; vertexIdx += 3, uvIdx += 2) {
            float x = pos.x + vertices[vertexIdx];
            float y = pos.y + vertices[vertexIdx + 1];
            float z = pos.z + vertices[vertexIdx + 2];

            // Calculate the UV coordinates from the texture region.
            float u = MathHelper.lerp(uvs[uvIdx], region.getU(), region.getU2());
            float v = MathHelper.lerp(uvs[uvIdx + 1], region.getV(), region.getV2());

            output.add(x);
            output.add(y);
            output.add(z);
            output.add(normal.x);
            output.add(normal.y);
            output.add(normal.z);
            output.add(u);
            output.add(v);
        }
    }

    public Model build(Texture texture, Material material) {
        /*
         *        +--------+
         *        |  top   |
         * +------+--------+-------+------+
         * | left | front  | right | back |
         * +------+--------+-------+------+
         *        | bottom |
         *        +-------+
         */

        FloatArray output = new FloatArray();
        int ix = (int) this.x;
        int iy = (int) this.y;
        int iz = (int) this.z;
        int ix2 = (int) (this.x + this.width);
        int iy2 = (int) (this.y + this.height);
        int iz2 = (int) (this.z + this.depth);
        int iw = (int) this.width;
        int ih = (int) this.height;
        int id = (int) this.depth;
        int iu = this.u;
        int iv = this.v;
        this.face(new Vec3i(ix, iy2, iz), BlockFace.TOP, new TextureRegion(texture, iu + id, iv, iw, id), output);
        this.face(new Vec3i(ix, iy, iz), BlockFace.BOTTOM, new TextureRegion(texture, iu + id, iv + id + ih, iw, id), output);

        this.face(new Vec3i(ix, iy, iz), BlockFace.LEFT, new TextureRegion(texture, iu, iv + id, id, ih), output);
        this.face(new Vec3i(ix2, iy, iz), BlockFace.RIGHT, new TextureRegion(texture, iu + id + iw, iv + id + ih, id, ih), output);

        this.face(new Vec3i(ix, iy, iz), BlockFace.FRONT, new TextureRegion(texture, iu + id, id + id, iw, ih), output);
        this.face(new Vec3i(ix, iy, iz2), BlockFace.BACK, new TextureRegion(texture, iu + id * 2 + iw, id + id), output);

        return UltracraftClient.invokeAndWait(() -> {
            var modelBuilder = new ModelBuilder();
            modelBuilder.begin();
            modelBuilder.part("cube",
                    new Mesh(false, output.size, 0, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)),
                    GL_TRIANGLES, material);
            return modelBuilder.end();
        });
    }
}

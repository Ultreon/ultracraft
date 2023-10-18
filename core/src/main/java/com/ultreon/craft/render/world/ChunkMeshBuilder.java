package com.ultreon.craft.render.world;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.FloatArray;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.world.Section;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static com.ultreon.craft.world.Chunk.VERTEX_SIZE;
import static com.ultreon.craft.world.World.CHUNK_SIZE;

public class ChunkMeshBuilder {
    private short[] indices;

    public ChunkMeshBuilder(short[] indices) {
        this.indices = indices;
    }

    @NotNull
    @CanIgnoreReturnValue
    @Contract("_, _ -> param1")
    ChunkMesh buildMesh(@NotNull ChunkMesh chunkMesh, @NotNull Section section) {
        Mesh mesh;
        var vertices = new FloatArray();
        var numVertices = this.buildVertices(section, vertices);
        mesh = new Mesh(false, false, numVertices,
                this.indices.length * 6, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)));
        mesh.setIndices(this.indices);
        mesh.setVertices(vertices.items);
        vertices.clear();
        section.setDirty(false);
        vertices.items = null;

        chunkMesh.meshPart.mesh = mesh;
        chunkMesh.meshPart.size = this.indices.length;
        chunkMesh.meshPart.offset = 0;
        chunkMesh.meshPart.primitiveType = GL_TRIANGLES;

        return chunkMesh;
    }

    /**
     * Creates a mesh out of the chunk, returning the number of indices produced
     *
     * @return the number of vertices produced
     */
    private int buildVertices(Section section, FloatArray vertices) {
        int i = 0;
        Vec3i offset = new Vec3i();

        for (int y = 0; y < CHUNK_SIZE; y++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int x = 0; x < CHUNK_SIZE; x++, i++) {
                    Block block = section.get(x, y, z);

                    if (block == Blocks.AIR) continue;

                    BakedCubeModel model = UltreonCraft.get().getBakedBlockModel(block);

                    if (model == null) continue;

                    if (y < CHUNK_SIZE - 1) {
                        this.getB(section, x, y + 1, z);
                        if (this.getB(section, x, y + 1, z) == Blocks.AIR || this.getB(section, x, y + 1, z).isTransparent())
                            ChunkMeshBuilder.createTop(offset, x, y, z, model.top(), vertices);
                    } else {
                        ChunkMeshBuilder.createTop(offset, x, y, z, model.top(), vertices);
                    }
                    if (y > 0) {
                        this.getB(section, x, y - 1, z);
                        if (this.getB(section, x, y - 1, z) == Blocks.AIR || this.getB(section, x, y - 1, z).isTransparent())
                            ChunkMeshBuilder.createBottom(offset, x, y, z, model.bottom(), vertices);
                    } else {
                        ChunkMeshBuilder.createBottom(offset, x, y, z, model.bottom(), vertices);
                    }
                    if (x > 0) {
                        this.getB(section, x - 1, y, z);
                        if (this.getB(section, x - 1, y, z) == Blocks.AIR || this.getB(section, x - 1, y, z).isTransparent())
                            ChunkMeshBuilder.createLeft(offset, x, y, z, model.left(), vertices);
                    } else {
                        ChunkMeshBuilder.createLeft(offset, x, y, z, model.left(), vertices);
                    }
                    if (x < CHUNK_SIZE - 1) {
                        this.getB(section, x + 1, y, z);
                        if (this.getB(section, x + 1, y, z) == Blocks.AIR || this.getB(section, x + 1, y, z).isTransparent())
                            ChunkMeshBuilder.createRight(offset, x, y, z, model.right(), vertices);
                    } else {
                        ChunkMeshBuilder.createRight(offset, x, y, z, model.right(), vertices);
                    }
                    if (z > 0) {
                        this.getB(section, x, y, z - 1);
                        if (this.getB(section, x, y, z - 1) == Blocks.AIR || this.getB(section, x, y, z - 1).isTransparent())
                            ChunkMeshBuilder.createFront(offset, x, y, z, model.front(), vertices);
                    } else {
                        ChunkMeshBuilder.createFront(offset, x, y, z, model.front(), vertices);
                    }
                    if (z < CHUNK_SIZE - 1) {
                        this.getB(section, x, y, z + 1);
                        if (this.getB(section, x, y, z + 1) == Blocks.AIR || this.getB(section, x, y, z + 1).isTransparent())
                            ChunkMeshBuilder.createBack(offset, x, y, z, model.back(), vertices);
                    } else {
                        ChunkMeshBuilder.createBack(offset, x, y, z, model.back(), vertices);
                    }
                }
            }
        }
        return vertices.size / VERTEX_SIZE + 1;
    }

    protected static void createTop(Vec3i offset, int x, int y, int z, TextureRegion region, FloatArray vertices) {
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

    protected static void createBottom(Vec3i offset, int x, int y, int z, TextureRegion region, FloatArray vertices) {
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

    protected static void createLeft(Vec3i offset, int x, int y, int z, TextureRegion region, FloatArray vertices) {
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

    protected static void createRight(Vec3i offset, int x, int y, int z, TextureRegion region, FloatArray vertices) {
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

    protected static void createFront(Vec3i offset, int x, int y, int z, TextureRegion region, FloatArray vertices) {
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

    protected static void createBack(Vec3i offset, int x, int y, int z, TextureRegion region, FloatArray vertices) {
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

    private Block getB(Section section, int x, int y, int z) {
//		return world.get(new Vec3i(pos.x * size + x, y, pos.z * size + z));
        return section.get(new Vec3i(x, y, z));
    }
}

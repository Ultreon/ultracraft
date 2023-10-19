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
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static com.ultreon.craft.world.World.*;

public class ChunkMeshBuilder {
    private static long meshesBuilt = 0L;
    private final short[] indices;

    public ChunkMeshBuilder(short[] indices) {
        this.indices = indices;
    }

    public static long getMeshesBuilt() {
        return meshesBuilt;
    }

    @NotNull
    @CanIgnoreReturnValue
    @Contract("_, _ -> param1")
    ChunkMesh buildMesh(@NotNull ChunkMesh chunkMesh, @NotNull Chunk section) {
        Mesh mesh;
        var vertices = new FloatArray();
        this.buildVertices(section, vertices);

        int vertexSize = vertices.items.length;
        int indexSize = vertexSize / 8;

        mesh = new Mesh(false, false, vertexSize,
                indexSize, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)));
        ChunkMeshBuilder.meshesBuilt++;
        mesh.setIndices(this.indices, 0, indexSize);
        mesh.setVertices(vertices.items, 0, vertexSize);
        vertices.items = null;
        vertices.clear();
        section.setDirty(false);

        WorldRenderer.vertexCount += mesh.getMaxVertices();

        chunkMesh.meshPart.mesh = mesh;
        chunkMesh.meshPart.size = indexSize > 0 ? indexSize : vertexSize;
        chunkMesh.meshPart.offset = 0;
        chunkMesh.meshPart.primitiveType = GL_TRIANGLES;

        return chunkMesh;
    }

    /**
     * Creates a mesh out of the chunk, returning the number of indices produced
     */
    private void buildVertices(Chunk section, FloatArray vertices) {
        int i = 0;

        for (int y = 0; y < CHUNK_HEIGHT; y++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int x = 0; x < CHUNK_SIZE; x++, i++) {
                    Block block = this.block(section, x, y, z);

                    if (block == null || block == Blocks.AIR) continue;

                    BakedCubeModel model = UltreonCraft.get().getBakedBlockModel(block);
                    if (block.isTransparent()) continue;

                    if (model == null) continue;
                    {
                        Block b = this.block(section, x, y + 1, z);
                        if (b != null && (b == Blocks.AIR || b.isTransparent()))
                            ChunkMeshBuilder.createTop(x, y, z, model.top(), vertices);
                    }
                    {
                        Block b = this.block(section, x, y - 1, z);
                        if (b != null && (b == Blocks.AIR || b.isTransparent())) {
                            ChunkMeshBuilder.createBottom(x, y, z, model.bottom(), vertices);
                        }
                    }
                    {
                        Block b = this.block(section, x - 1, y, z);
                        if (b != null && (b == Blocks.AIR || b.isTransparent()))
                            ChunkMeshBuilder.createLeft(x, y, z, model.left(), vertices);
                    }
                    {
                        Block b = this.block(section, x + 1, y, z);
                        if (b != null && (b == Blocks.AIR || b.isTransparent()))
                            ChunkMeshBuilder.createRight(x, y, z, model.right(), vertices);
                    }
                    {
                        Block b = this.block(section, x, y, z - 1);
                        if (b != null && (b == Blocks.AIR || b.isTransparent()))
                            ChunkMeshBuilder.createFront(x, y, z, model.front(), vertices);
                    }
                    {
                        Block b = this.block(section, x, y, z + 1);
                        if (b != null && (b == Blocks.AIR || b.isTransparent()))
                            ChunkMeshBuilder.createBack(x, y, z, model.back(), vertices);
                    }
                }
            }
        }
    }

    @NotNull
    @CanIgnoreReturnValue
    @Contract("_, _ -> param1")
    ChunkMesh buildTransparentMesh(@NotNull ChunkMesh chunkMesh, @NotNull Chunk section) {
        var vertices = new FloatArray();
        this.buildTransparentVertices(section, vertices);

        Mesh mesh = new Mesh(false, false, vertices.items.length,
                this.indices.length * 6, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)));

        ChunkMeshBuilder.meshesBuilt++;

        mesh.setIndices(this.indices);
        mesh.setVertices(vertices.items);
        vertices.clear();
        section.dirty = false;
        vertices.items = null;

        WorldRenderer.vertexCount += mesh.getMaxVertices();

        chunkMesh.meshPart.mesh = mesh;
        chunkMesh.meshPart.size = this.indices.length;
        chunkMesh.meshPart.offset = 0;
        chunkMesh.meshPart.primitiveType = GL_TRIANGLES;

        return chunkMesh;
    }

    /**
     * Creates a mesh out of the chunk, returning the number of indices produced
     */
    private void buildTransparentVertices(Chunk section, FloatArray vertices) {
        int i = 0;
        for (int y = 0; y < CHUNK_HEIGHT; y++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int x = 0; x < CHUNK_SIZE; x++, i++) {
                    Block block = this.block(section, x, y, z);

                    if (block == null || block == Blocks.AIR) continue;
                    if (!block.isTransparent()) continue;
                    if (true) continue;

                    BakedCubeModel model = UltreonCraft.get().getBakedBlockModel(block);

                    if (model == null) continue;

                    if (y < CHUNK_HEIGHT - 1) {
                        this.block(section, x, y + 1, z);
                        if (this.block(section, x, y + 1, z) == Blocks.AIR && !this.block(section, x, y + 1, z).isTransparent())
                            ChunkMeshBuilder.createTop(x, y, z, model.top(), vertices);
                    } else {
                        ChunkMeshBuilder.createTop(x, y, z, model.top(), vertices);
                    }
                    if (y > 0) {
                        this.block(section, x, y - 1, z);
                        if (this.block(section, x, y - 1, z) == Blocks.AIR && !this.block(section, x, y - 1, z).isTransparent())
                            ChunkMeshBuilder.createBottom(x, y, z, model.bottom(), vertices);
                    } else {
                        ChunkMeshBuilder.createBottom(x, y, z, model.bottom(), vertices);
                    }
                    if (x > 0) {
                        this.block(section, x - 1, y, z);
                        if (this.block(section, x - 1, y, z) == Blocks.AIR && !this.block(section, x - 1, y, z).isTransparent())
                            ChunkMeshBuilder.createLeft(x, y, z, model.left(), vertices);
                    } else {
                        ChunkMeshBuilder.createLeft(x, y, z, model.left(), vertices);
                    }
                    if (x < CHUNK_SIZE - 1) {
                        this.block(section, x + 1, y, z);
                        if (this.block(section, x + 1, y, z) == Blocks.AIR && !this.block(section, x + 1, y, z).isTransparent())
                            ChunkMeshBuilder.createRight(x, y, z, model.right(), vertices);
                    } else {
                        ChunkMeshBuilder.createRight(x, y, z, model.right(), vertices);
                    }
                    if (z > 0) {
                        this.block(section, x, y, z - 1);
                        if (this.block(section, x, y, z - 1) == Blocks.AIR && !this.block(section, x, y, z - 1).isTransparent())
                            ChunkMeshBuilder.createFront(x, y, z, model.front(), vertices);
                    } else {
                        ChunkMeshBuilder.createFront(x, y, z, model.front(), vertices);
                    }
                    if (z < CHUNK_SIZE - 1) {
                        this.block(section, x, y, z + 1);
                        if (this.block(section, x, y, z + 1) == Blocks.AIR && !this.block(section, x, y, z + 1).isTransparent())
                            ChunkMeshBuilder.createBack(x, y, z, model.back(), vertices);
                    } else {
                        ChunkMeshBuilder.createBack(x, y, z, model.back(), vertices);
                    }
                }
            }
        }
    }

    protected static void createTop(int x, int y, int z, TextureRegion region, FloatArray vertices) {
        vertices.add(x);
        vertices.add(y + 1);
        vertices.add(z);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(x + 1);
        vertices.add(y + 1);
        vertices.add(z);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());

        vertices.add(x + 1);
        vertices.add(y + 1);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(x);
        vertices.add(y + 1);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());
    }

    protected static void createBottom(int x, int y, int z, TextureRegion region, FloatArray vertices) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(x);
        vertices.add(y);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());

        vertices.add(x + 1);
        vertices.add(y);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(x + 1);
        vertices.add(y);
        vertices.add(z);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());
    }

    protected static void createLeft(int x, int y, int z, TextureRegion region, FloatArray vertices) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(x);
        vertices.add(y + 1);
        vertices.add(z);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());

        vertices.add(x);
        vertices.add(y + 1);
        vertices.add(z + 1);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(x);
        vertices.add(y);
        vertices.add(z + 1);
        vertices.add(-1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());
    }

    protected static void createRight(int x, int y, int z, TextureRegion region, FloatArray vertices) {
        vertices.add(x + 1);
        vertices.add(y);
        vertices.add(z);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(x + 1);
        vertices.add(y);
        vertices.add(z + 1);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV2());

        vertices.add(x + 1);
        vertices.add(y + 1);
        vertices.add(z + 1);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(x + 1);
        vertices.add(y + 1);
        vertices.add(z);
        vertices.add(1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(region.getU2());
        vertices.add(region.getV());
    }

    protected static void createFront(int x, int y, int z, TextureRegion region, FloatArray vertices) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(x + 1);
        vertices.add(y);
        vertices.add(z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU());
        vertices.add(region.getV2());

        vertices.add(x + 1);
        vertices.add(y + 1);
        vertices.add(z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(x);
        vertices.add(y + 1);
        vertices.add(z);
        vertices.add(0);
        vertices.add(0);
        vertices.add(1);
        vertices.add(region.getU2());
        vertices.add(region.getV());
    }

    protected static void createBack(int x, int y, int z, TextureRegion region, FloatArray vertices) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU2());
        vertices.add(region.getV2());

        vertices.add(x);
        vertices.add(y + 1);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU2());
        vertices.add(region.getV());

        vertices.add(x + 1);
        vertices.add(y + 1);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU());
        vertices.add(region.getV());

        vertices.add(x + 1);
        vertices.add(y);
        vertices.add(z + 1);
        vertices.add(0);
        vertices.add(0);
        vertices.add(-1);
        vertices.add(region.getU());
        vertices.add(region.getV2());
    }

    private Block block(Chunk chunk, int x, int y, int z) {
//		return world.get(new Vec3i(pos.x * size + x, y, pos.z * size + z));
        if (y < WORLD_DEPTH) return null;
        World world = chunk.getWorld();
        Vec3i pos = new Vec3i(chunk.pos.x(), 0, chunk.pos.z()).mul(16).add(x, y, z);
        Chunk chunkAt = world.getChunkAt(pos);
        if (chunkAt != null) {
            return world.get(pos);
        }
        return null;
    }
}

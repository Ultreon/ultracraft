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
    private final short[] indices;

    public ChunkMeshBuilder(short[] indices) {
        this.indices = indices;
    }

    @NotNull
    @CanIgnoreReturnValue
    @Contract("_, _ -> param1")
    ChunkMesh buildMesh(@NotNull ChunkMesh chunkMesh, @NotNull Chunk section) {
        Mesh mesh;
        var vertices = new FloatArray();
        this.buildVertices(section, vertices);
        mesh = new Mesh(false, false, vertices.items.length,
                this.indices.length * 6, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)));
        mesh.setIndices(this.indices);
        mesh.setVertices(vertices.items);
        vertices.clear();
        section.setDirty(false);
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
    private void buildVertices(Chunk section, FloatArray vertices) {
        int i = 0;
        Vec3i offset = new Vec3i();

        for (int y = 0; y < CHUNK_HEIGHT; y++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int x = 0; x < CHUNK_SIZE; x++, i++) {
                    Block block = section.get(x, y, z);

                    if (block == Blocks.AIR) continue;

                    BakedCubeModel model = UltreonCraft.get().getBakedBlockModel(block);
                    if (block.isTransparent()) continue;

                    if (model == null) continue;

                    {
                        Block b = this.getB(section, x, y + 1, z);
                        if ((b == null || b == Blocks.AIR) || b.isTransparent())
                            ChunkMeshBuilder.createTop(offset, x, y, z, model.top(), vertices);
                    }
                    {
                        Block b = this.getB(section, x, y - 1, z);
                        if ((b == null || b == Blocks.AIR) || b.isTransparent())
                            ChunkMeshBuilder.createBottom(offset, x, y, z, model.bottom(), vertices);
                    }
                    {
                        Block b = this.getB(section, x - 1, y, z);
                        if ((b == null || b == Blocks.AIR) || b.isTransparent())
                            ChunkMeshBuilder.createLeft(offset, x, y, z, model.left(), vertices);
                    }
                    {
                        Block b = this.getB(section, x + 1, y, z);
                        if ((b == null || b == Blocks.AIR) || b.isTransparent())
                            ChunkMeshBuilder.createRight(offset, x, y, z, model.right(), vertices);
                    }
                    {
                        Block b = this.getB(section, x, y, z - 1);
                        if ((b == null || b == Blocks.AIR) || b.isTransparent())
                            ChunkMeshBuilder.createFront(offset, x, y, z, model.front(), vertices);
                    }
                    {
                        Block b = this.getB(section, x, y, z + 1);
                        if ((b == null || b == Blocks.AIR) || b.isTransparent())
                            ChunkMeshBuilder.createBack(offset, x, y, z, model.back(), vertices);
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
        Vec3i offset = new Vec3i();

        for (int y = 0; y < CHUNK_HEIGHT; y++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int x = 0; x < CHUNK_SIZE; x++, i++) {
                    Block block = section.get(x, y, z);

                    if (block == Blocks.AIR) continue;
                    if (!block.isTransparent()) continue;

                    BakedCubeModel model = UltreonCraft.get().getBakedBlockModel(block);

                    if (model == null) continue;

                    {
                        Block b = this.getB(section, x, y + 1, z);
                        if (b == Blocks.AIR && !b.isTransparent())
                            ChunkMeshBuilder.createTop(offset, x, y, z, model.top(), vertices);
                    }
                    {
                        Block b = this.getB(section, x, y - 1, z);
                        if (b == Blocks.AIR && !b.isTransparent())
                            ChunkMeshBuilder.createBottom(offset, x, y, z, model.bottom(), vertices);
                    }
                    {
                        Block b = this.getB(section, x - 1, y, z);
                        if (b == Blocks.AIR && !b.isTransparent())
                            ChunkMeshBuilder.createLeft(offset, x, y, z, model.left(), vertices);
                    }
                    {
                        Block b = this.getB(section, x + 1, y, z);
                        if (b == Blocks.AIR && !b.isTransparent())
                            ChunkMeshBuilder.createRight(offset, x, y, z, model.right(), vertices);
                    }
                    {
                        Block b = this.getB(section, x, y, z - 1);
                        if (b == Blocks.AIR && !b.isTransparent())
                            ChunkMeshBuilder.createFront(offset, x, y, z, model.front(), vertices);
                    }
                    {
                        Block b = this.getB(section, x, y, z + 1);
                        if (b == Blocks.AIR && !b.isTransparent())
                            ChunkMeshBuilder.createBack(offset, x, y, z, model.back(), vertices);
                    }
                }
            }
        }
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

    private Block getB(Chunk chunk, int x, int y, int z) {
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

package com.ultreon.craft.client.world;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.model.BakedCubeModel;
import com.ultreon.craft.util.MathHelper;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.Chunk;
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
    ChunkMesh buildMesh(@NotNull ChunkMesh chunkMesh, @NotNull ClientChunk chunk) {
        Mesh mesh;
        var vertices = new FloatArray();
        this.buildVertices(chunk, vertices);
        mesh = new Mesh(false, false, vertices.items.length,
                this.indices.length * 6, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)));
        mesh.setIndices(this.indices);
        mesh.setVertices(vertices.items);
        vertices.clear();
        chunk.setDirty(false);
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
    private void buildVertices(ClientChunk chunk, FloatArray vertices) {
        int i = 0;

        for (int y = 0; y < CHUNK_HEIGHT; y++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int x = 0; x < CHUNK_SIZE; x++, i++) {
                    Block block = chunk.get(x, y, z);
                    Vec3i offset = new Vec3i(x, y, z);

                    if (!block.doesRender()) continue;

                    BakedCubeModel model = UltracraftClient.get().getBakedBlockModel(block);
                    if (block.isTransparent()) continue;

                    if (model == null) continue;

                    {
                        Block b = this.block(chunk, x, y + 1, z);
                        if (b == null || b == Blocks.AIR || b.isTransparent() || !b.doesRender())
                            ChunkMeshBuilder.drawFace(offset, BlockFace.TOP, model.top(), vertices, model.properties.top);
                    }
                    {
                        Block b = this.block(chunk, x, y - 1, z);
                        if (b == null || b == Blocks.AIR || b.isTransparent() || !b.doesRender())
                            ChunkMeshBuilder.drawFace(offset, BlockFace.BOTTOM, model.bottom(), vertices, model.properties.bottom);
                    }
                    {
                        Block b = this.block(chunk, x - 1, y, z);
                        if (b == null || b == Blocks.AIR || b.isTransparent() || !b.doesRender())
                            ChunkMeshBuilder.drawFace(offset, BlockFace.LEFT, model.left(), vertices, model.properties.left);
                    }
                    {
                        Block b = this.block(chunk, x + 1, y, z);
                        if (b == null || b == Blocks.AIR || b.isTransparent() || !b.doesRender())
                            ChunkMeshBuilder.drawFace(offset, BlockFace.RIGHT, model.right(), vertices, model.properties.right);
                    }
                    {
                        Block b = this.block(chunk, x, y, z - 1);
                        if (b == null || b == Blocks.AIR || b.isTransparent() || !b.doesRender())
                            ChunkMeshBuilder.drawFace(offset, BlockFace.FRONT, model.front(), vertices, model.properties.front);
                    }
                    {
                        Block b = this.block(chunk, x, y, z + 1);
                        if (b == null || b == Blocks.AIR || b.isTransparent() || !b.doesRender())
                            ChunkMeshBuilder.drawFace(offset, BlockFace.BACK, model.back(), vertices, model.properties.back);
                    }
                }
            }
        }
    }

    @NotNull
    @CanIgnoreReturnValue
    @Contract("_, _ -> param1")
    ChunkMesh buildTransparentMesh(@NotNull ChunkMesh chunkMesh, @NotNull ClientChunk chunk) {
        var vertices = new FloatArray();
        this.buildTransparentVertices(chunk, vertices);

        Mesh mesh = new Mesh(false, false, vertices.items.length,
                this.indices.length * 6, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)));

        mesh.setIndices(this.indices);
        mesh.setVertices(vertices.items);
        vertices.clear();
        chunk.setDirty(false);
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
    private void buildTransparentVertices(ClientChunk section, FloatArray vertices) {
        int i = 0;

        for (int y = 0; y < CHUNK_HEIGHT; y++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int x = 0; x < CHUNK_SIZE; x++, i++) {
                    Block block = section.get(x, y, z);
                    Vec3i offset = new Vec3i(x, y, z);

                    if (!block.doesRender()) continue;
                    if (!block.isTransparent()) continue;

                    BakedCubeModel model = UltracraftClient.get().getBakedBlockModel(block);

                    if (model == null) continue;

                    {
                        Block b = this.block(section, x, y + 1, z);
                        if (b == null || b == Blocks.AIR && !b.isTransparent() || !b.doesRender())
                            ChunkMeshBuilder.drawFace(offset, BlockFace.TOP, model.top(), vertices, model.properties.top);
                    }
                    {
                        Block b = this.block(section, x, y - 1, z);
                        if (b == null || b == Blocks.AIR && !b.isTransparent() || !b.doesRender())
                            ChunkMeshBuilder.drawFace(offset, BlockFace.BOTTOM, model.bottom(), vertices, model.properties.bottom);
                    }
                    {
                        Block b = this.block(section, x - 1, y, z);
                        if (b == null || b == Blocks.AIR && !b.isTransparent() || !b.doesRender())
                            ChunkMeshBuilder.drawFace(offset, BlockFace.LEFT, model.left(), vertices, model.properties.left);
                    }
                    {
                        Block b = this.block(section, x + 1, y, z);
                        if (b == null || b == Blocks.AIR && !b.isTransparent() || !b.doesRender())
                            ChunkMeshBuilder.drawFace(offset, BlockFace.RIGHT, model.right(), vertices, model.properties.right);
                    }
                    {
                        Block b = this.block(section, x, y, z - 1);
                        if (b == null || b == Blocks.AIR && !b.isTransparent() || !b.doesRender())
                            ChunkMeshBuilder.drawFace(offset, BlockFace.FRONT, model.front(), vertices, model.properties.front);
                    }
                    {
                        Block b = this.block(section, x, y, z + 1);
                        if (b == null || b == Blocks.AIR && !b.isTransparent() || !b.doesRender())
                            ChunkMeshBuilder.drawFace(offset, BlockFace.BACK, model.back(), vertices, model.properties.back);
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

    static float[] backUv = {
            1, 1,
            1, 0,
            0, 0,
            0, 1,
    };

    static float[] frontUv = {
            1, 1,
            0, 1,
            0, 0,
            1, 0,
    };

    static float[] rightUv = {
            1, 1,
            0, 1,
            0, 0,
            1, 0,
    };

    static float[] leftUv = {
            1, 1,
            1, 0,
            0, 0,
            0, 1,
    };

    static float[] bottomUv = {
            0, 0,
            0, 1,
            1, 1,
            0, 1,
    };

    static float[] topUv = {
            0, 0,
            1, 0,
            1, 1,
            0, 1
    };

    static float[] topVertices = {
            0, 1, 0,
            1, 1, 0,
            1, 1, 1,
            0, 1, 1
    };

    static float[] bottomVertices = {
            0, 0, 0,
            0, 0, 1,
            1, 0, 1,
            1, 0, 0
    };

    static float[] leftVertices = {
            0, 0, 0,
            0, 1, 0,
            0, 1, 1,
            0, 0, 1
    };

    static float[] rightVertices = {
            1, 0, 0,
            1, 0, 1,
            1, 1, 1,
            1, 1, 0
    };

    static float[] frontVertices = {
            0, 0, 0,
            1, 0, 0,
            1, 1, 0,
            0, 1, 0,
    };

    static float[] backVertices = {
        0, 0, 1,
        0, 1, 1,
        1, 1, 1,
        1, 0, 1,
    };

    public static void drawFace(Vec3i offset, BlockFace face, TextureRegion region, FloatArray output, FaceProperties faceProperties) {
        float[] vertices = switch(face) {
            case TOP -> ChunkMeshBuilder.topVertices;
            case BOTTOM -> ChunkMeshBuilder.bottomVertices;
            case LEFT -> ChunkMeshBuilder.leftVertices;
            case RIGHT -> ChunkMeshBuilder.rightVertices;
            case FRONT -> ChunkMeshBuilder.frontVertices;
            case BACK -> ChunkMeshBuilder.backVertices;
        };
        
        float[] uvs = switch (face) {
            case TOP -> ChunkMeshBuilder.topUv;
            case BOTTOM -> ChunkMeshBuilder.bottomUv;
            case LEFT -> ChunkMeshBuilder.leftUv;
            case RIGHT -> ChunkMeshBuilder.rightUv;
            case FRONT -> ChunkMeshBuilder.frontUv;
            case BACK -> ChunkMeshBuilder.backUv;
        };

        Vector3 normal = face.getNormal();

//        if (faceProperties.randomRotation) {
//            // Generate seed for offset.
//            long seed = (long) face.ordinal() + (long) offset.x + (long) offset.y + (long) offset.z;
//            Random random = new Random(seed);
//
//            vertices = MathHelper.rotate(vertices, random);
//        }

        // Loop vertices and uvs and add them to the output.
        for (int vertex = 0, uv = 0; vertex < vertices.length; vertex += 3, uv += 2) {
            float x = offset.x + vertices[vertex];
            float y = offset.y + vertices[vertex + 1];
            float z = offset.z + vertices[vertex + 2];

            // Calculate the UV coordinates from the texture region.
            float u = MathHelper.lerp(uvs[uv], region.getU(), region.getU2());
            float v = MathHelper.lerp(uvs[uv + 1], region.getV(), region.getV2());
            
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

    private Block block(ClientChunk chunk, int x, int y, int z) {
        if (y < WORLD_DEPTH) return null;
        ClientWorld world = chunk.getWorld();
        Vec3i vec = new Vec3i(chunk.getPos().x(), 0, chunk.getPos().z()).mul(16).add(x, y, z);
        BlockPos pos = new BlockPos(vec);
        Chunk chunkAt = world.getChunkAt(pos);
        if (chunkAt != null) {
            return world.get(pos);
        }
        return null;
    }

}

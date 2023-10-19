package com.ultreon.craft.render.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.world.Chunk;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static com.ultreon.craft.world.World.CHUNK_HEIGHT;
import static com.ultreon.craft.world.World.CHUNK_SIZE;

public class ChunkMeshBuilder {
    private static final float[] EMPTY = new float[0];
    private static final Color COLOR = Color.WHITE;
    private static final Vector3 NORMAL_TOP = new Vector3(0, 1, 0);
    private static final Vector3 NORMAL_BOTTOM = new Vector3(0, -1, 0);
    private static final Vector3 NORMAL_LEFT = new Vector3(-1, 0, 0);
    private static final Vector3 NORMAL_RIGHT = new Vector3(1, 0, 0);
    private static final Vector3 NORMAL_FRONT = new Vector3(0, 0, 1);
    private static final Vector3 NORMAL_BACK = new Vector3(0, 0, -1);
    private final VertexAttributes attributes = new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
    private static long meshesBuilt = 0L;
    private final short[] indices;
    private final Vector3 tmp = new Vector3();
    private final Vector3 pos = new Vector3();

    public ChunkMeshBuilder(short[] indices) {
        this.indices = indices;
    }

    public static Object getMeshesBuilt() {
        return ChunkMeshBuilder.meshesBuilt;
    }

    @NotNull
    @NewInstance
    @CanIgnoreReturnValue
    public ChunkMesh buildChunk(ChunkMesh chunkMesh, Chunk chunk) {
        FloatArray vertices = new FloatArray();
        this.buildMesh(chunk, vertices);
        Mesh mesh = new Mesh(true, true, Math.max(vertices.size, 64), this.indices.length, this.attributes);
        mesh.setVertices(vertices.items);
        mesh.setIndices(this.indices);
        vertices.items = null;
        vertices.clear();

        chunk.setDirty(false);

        try {
            Thread.sleep((long) Gdx.graphics.getDeltaTime() * 4);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        int maxVertices = mesh.getMaxVertices();
        int maxIndices = mesh.getMaxIndices();

        chunkMesh.renderable.userData = chunk;
        chunkMesh.meshPart.mesh = mesh;
        chunkMesh.meshPart.size = maxIndices > 0 ? maxIndices : maxVertices;
        chunkMesh.meshPart.offset = 0;
        chunkMesh.meshPart.primitiveType = GL_TRIANGLES;
        ChunkMeshBuilder.meshesBuilt++;

        return chunkMesh;
    }

    /**
     * Creates a mesh out of the chunk, returning the number of indices produced
     */
    private void buildMesh(Chunk chunk, FloatArray vertices) {
        int i = 0;

        for (int y = 0; y < CHUNK_SIZE; y++) {
            for (int z = 0; z < CHUNK_HEIGHT; z++) {
                for (int x = 0; x < CHUNK_SIZE; x++, i++) {
//        boolean flag = true;
//        while (flag) {
//            flag = false;
//            var x = 8;
//            var y = 8;
//            var z = 8;

            Block block = chunk.get(x, y, z);

            if (block == Blocks.AIR) continue;

            BakedCubeModel model = UltreonCraft.get().getBakedBlockModel(block);

            if (model == null) continue;

            this.pos.set(x, y, z);

            if (y < CHUNK_SIZE - 1) {
                this.getB(chunk, x, y + 1, z);
                if (this.getB(chunk, x, y + 1, z) == Blocks.AIR || this.getB(chunk, x, y + 1, z).isTransparent())
                    this.face(this.pos, ChunkMeshBuilder.NORMAL_TOP, this.top, this.topUV, model.top(), vertices);
            } else {
                this.face(this.pos, ChunkMeshBuilder.NORMAL_TOP, this.top, this.topUV, model.top(), vertices);
            }
            if (y > 0) {
                this.getB(chunk, x, y - 1, z);
                if (this.getB(chunk, x, y - 1, z) == Blocks.AIR || this.getB(chunk, x, y - 1, z).isTransparent())
                    this.face(this.pos, ChunkMeshBuilder.NORMAL_BOTTOM, this.bottom, this.bottomUV, model.bottom(), vertices);
            } else {
                this.face(this.pos, ChunkMeshBuilder.NORMAL_BOTTOM, this.bottom, this.bottomUV, model.bottom(), vertices);
            }
            if (x > 0) {
                this.getB(chunk, x - 1, y, z);
                if (this.getB(chunk, x - 1, y, z) == Blocks.AIR || this.getB(chunk, x - 1, y, z).isTransparent())
                    this.face(this.pos, ChunkMeshBuilder.NORMAL_LEFT, this.left, this.leftUV, model.left(), vertices);
            } else {
                this.face(this.pos, ChunkMeshBuilder.NORMAL_LEFT, this.left, this.leftUV, model.left(), vertices);
            }
            if (x < CHUNK_SIZE - 1) {
                this.getB(chunk, x + 1, y, z);
                if (this.getB(chunk, x + 1, y, z) == Blocks.AIR || this.getB(chunk, x + 1, y, z).isTransparent())
                    this.face(this.pos, ChunkMeshBuilder.NORMAL_RIGHT, this.right, this.rightUV, model.right(), vertices);
            } else {
                this.face(this.pos, ChunkMeshBuilder.NORMAL_RIGHT, this.right, this.rightUV, model.right(), vertices);
            }
            if (z > 0) {
                this.getB(chunk, x, y, z - 1);
                if (this.getB(chunk, x, y, z - 1) == Blocks.AIR || this.getB(chunk, x, y, z - 1).isTransparent())
                    this.face(this.pos, ChunkMeshBuilder.NORMAL_FRONT, this.front, this.frontUV, model.front(), vertices);
            } else {
                this.face(this.pos, ChunkMeshBuilder.NORMAL_FRONT, this.front, this.frontUV, model.front(), vertices);
            }
            if (z < CHUNK_SIZE - 1) {
                this.getB(chunk, x, y, z + 1);
                if (this.getB(chunk, x, y, z + 1) == Blocks.AIR || this.getB(chunk, x, y, z + 1).isTransparent())
                    this.face(this.pos, ChunkMeshBuilder.NORMAL_BACK, this.back, this.backUV, model.back(), vertices);
            } else {
                this.face(this.pos, ChunkMeshBuilder.NORMAL_BACK, this.back, this.backUV, model.back(), vertices);
            }
//        }
                }
            }
        }
    }

    protected void face(Vector3 pos, Vector3 normal, Vector3[] face, Vector2[] uv, TextureRegion region, FloatArray vertices) {
        float u = region.getU();
        float v = region.getV();
        float u2 = region.getU2();
        float v2 = region.getV2();

        System.out.println("pos = " + pos + ", normal = " + normal + ", region = " + region + ", uv = " + Arrays.toString(uv));

        Preconditions.checkArgument(face.length == uv.length, "Face and UV have different lengths");

        for (int idx = 0; idx < face.length; idx += 2) {
            vertices.add(pos.x + face[idx].x);
            vertices.add(pos.y + face[idx].y);
            vertices.add(pos.z + face[idx].z);
            vertices.add(normal.x);
            vertices.add(normal.y);
            vertices.add(normal.z);
            vertices.add(u + (u2 - u) * uv[idx].x);
            vertices.add(v + (v2 - v) * uv[idx].y);
        }
    }

    Vector3[] top = {
            new Vector3(0, 1, 0),
            new Vector3(1, 1, 0),
            new Vector3(1, 1, 1),
            new Vector3(0, 1, 1)
    };

    Vector3[] bottom = {
            new Vector3(),
            new Vector3(),
            new Vector3(),
            new Vector3()
    };

    Vector3[] left = {
            new Vector3(),
            new Vector3(),
            new Vector3(),
            new Vector3()
    };

    Vector3[] right = {
            new Vector3(),
            new Vector3(),
            new Vector3(),
            new Vector3()
    };

    Vector3[] front = {
            new Vector3(),
            new Vector3(),
            new Vector3(),
            new Vector3()
    };

    Vector3[] back = {
            new Vector3(),
            new Vector3(),
            new Vector3(),
            new Vector3()
    };

//    Vector3[] bottom = {
//            new Vector3(0, 0, 0),
//            new Vector3(0, 0, 1),
//            new Vector3(1, 0, 1),
//            new Vector3(1, 0, 0)
//    };
//
//    Vector3[] left = {
//            new Vector3(0, 0, 0),
//            new Vector3(0, 1, 0),
//            new Vector3(0, 1, 1),
//            new Vector3(0, 0, 1)
//    };
//
//    Vector3[] right = {
//            new Vector3(1, 0, 0),
//            new Vector3(1, 0, 1),
//            new Vector3(1, 1, 1),
//            new Vector3(1, 1, 0)
//    };
//
//    Vector3[] front = {
//            new Vector3(0, 0, 0),
//            new Vector3(1, 0, 0),
//            new Vector3(1, 1, 0),
//            new Vector3(0, 1, 0)
//    };
//
//    Vector3[] back = {
//            new Vector3(0, 0, 1),
//            new Vector3(0, 1, 1),
//            new Vector3(1, 1, 1),
//            new Vector3(1, 0, 1)
//    };

    Vector2[] topUV = {
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(1, 1),
            new Vector2(0, 1)
    };

    Vector2[] bottomUV = {
            new Vector2(0, 0),
            new Vector2(0, 1),
            new Vector2(1, 1),
            new Vector2(1, 0)
    };

    Vector2[] leftUV = {
            new Vector2(1, 1),
            new Vector2(1, 0),
            new Vector2(0, 0),
            new Vector2(0, 1),
    };

    Vector2[] rightUV = {
            new Vector2(1, 1),
            new Vector2(0, 1),
            new Vector2(0, 0),
            new Vector2(1, 0)
    };

    Vector2[] frontUV = {
            new Vector2(1, 1),
            new Vector2(0, 1),
            new Vector2(0, 0),
            new Vector2(1, 0)
    };

    Vector2[] backUV = {
            new Vector2(1, 1),
            new Vector2(1, 0),
            new Vector2(0, 0),
            new Vector2(0, 1)
    };

//    protected static void createTop(Vec3i offset, int x, int y, int z, TextureRegion region, FloatArray vertices) {
//        vertices.add(offset.x + x);
//        vertices.add(offset.y + y + 1);
//        vertices.add(offset.z + z);
//        vertices.add(0);
//        vertices.add(1);
//        vertices.add(0);
//        vertices.add(region.getU());
//        vertices.add(region.getV());
//
//        vertices.add(offset.x + x + 1);
//        vertices.add(offset.y + y + 1);
//        vertices.add(offset.z + z);
//        vertices.add(0);
//        vertices.add(1);
//        vertices.add(0);
//        vertices.add(region.getU2());
//        vertices.add(region.getV());
//
//        vertices.add(offset.x + x + 1);
//        vertices.add(offset.y + y + 1);
//        vertices.add(offset.z + z + 1);
//        vertices.add(0);
//        vertices.add(1);
//        vertices.add(0);
//        vertices.add(region.getU2());
//        vertices.add(region.getV2());
//
//        vertices.add(offset.x + x);
//        vertices.add(offset.y + y + 1);
//        vertices.add(offset.z + z + 1);
//        vertices.add(0);
//        vertices.add(1);
//        vertices.add(0);
//        vertices.add(region.getU());
//        vertices.add(region.getV2());
//    }
//
//    protected static void createBottom(Vec3i offset, int x, int y, int z, TextureRegion region, FloatArray vertices) {
//        vertices.add(offset.x + x);
//        vertices.add(offset.y + y);
//        vertices.add(offset.z + z);
//        vertices.add(0);
//        vertices.add(-1);
//        vertices.add(0);
//        vertices.add(region.getU());
//        vertices.add(region.getV());
//
//        vertices.add(offset.x + x);
//        vertices.add(offset.y + y);
//        vertices.add(offset.z + z + 1);
//        vertices.add(0);
//        vertices.add(-1);
//        vertices.add(0);
//        vertices.add(region.getU());
//        vertices.add(region.getV2());
//
//        vertices.add(offset.x + x + 1);
//        vertices.add(offset.y + y);
//        vertices.add(offset.z + z + 1);
//        vertices.add(0);
//        vertices.add(-1);
//        vertices.add(0);
//        vertices.add(region.getU2());
//        vertices.add(region.getV2());
//
//        vertices.add(offset.x + x + 1);
//        vertices.add(offset.y + y);
//        vertices.add(offset.z + z);
//        vertices.add(0);
//        vertices.add(-1);
//        vertices.add(0);
//        vertices.add(region.getU2());
//        vertices.add(region.getV());
//    }
//
//    protected static void createLeft(Vec3i offset, int x, int y, int z, TextureRegion region, FloatArray vertices) {
//        vertices.add(offset.x + x);
//        vertices.add(offset.y + y);
//        vertices.add(offset.z + z);
//        vertices.add(-1);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(region.getU2());
//        vertices.add(region.getV2());
//
//        vertices.add(offset.x + x);
//        vertices.add(offset.y + y + 1);
//        vertices.add(offset.z + z);
//        vertices.add(-1);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(region.getU2());
//        vertices.add(region.getV());
//
//        vertices.add(offset.x + x);
//        vertices.add(offset.y + y + 1);
//        vertices.add(offset.z + z + 1);
//        vertices.add(-1);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(region.getU());
//        vertices.add(region.getV());
//
//        vertices.add(offset.x + x);
//        vertices.add(offset.y + y);
//        vertices.add(offset.z + z + 1);
//        vertices.add(-1);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(region.getU());
//        vertices.add(region.getV2());
//    }
//
//    protected static void createRight(Vec3i offset, int x, int y, int z, TextureRegion region, FloatArray vertices) {
//        vertices.add(offset.x + x + 1);
//        vertices.add(offset.y + y);
//        vertices.add(offset.z + z);
//        vertices.add(1);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(region.getU2());
//        vertices.add(region.getV2());
//
//        vertices.add(offset.x + x + 1);
//        vertices.add(offset.y + y);
//        vertices.add(offset.z + z + 1);
//        vertices.add(1);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(region.getU());
//        vertices.add(region.getV2());
//
//        vertices.add(offset.x + x + 1);
//        vertices.add(offset.y + y + 1);
//        vertices.add(offset.z + z + 1);
//        vertices.add(1);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(region.getU());
//        vertices.add(region.getV());
//
//        vertices.add(offset.x + x + 1);
//        vertices.add(offset.y + y + 1);
//        vertices.add(offset.z + z);
//        vertices.add(1);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(region.getU2());
//        vertices.add(region.getV());
//    }
//
//    protected static void createFront(Vec3i offset, int x, int y, int z, TextureRegion region, FloatArray vertices) {
//        vertices.add(offset.x + x);
//        vertices.add(offset.y + y);
//        vertices.add(offset.z + z);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(1);
//        vertices.add(region.getU2());
//        vertices.add(region.getV2());
//
//        vertices.add(offset.x + x + 1);
//        vertices.add(offset.y + y);
//        vertices.add(offset.z + z);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(1);
//        vertices.add(region.getU());
//        vertices.add(region.getV2());
//
//        vertices.add(offset.x + x + 1);
//        vertices.add(offset.y + y + 1);
//        vertices.add(offset.z + z);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(1);
//        vertices.add(region.getU());
//        vertices.add(region.getV());
//
//        vertices.add(offset.x + x);
//        vertices.add(offset.y + y + 1);
//        vertices.add(offset.z + z);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(1);
//        vertices.add(region.getU2());
//        vertices.add(region.getV());
//    }
//
//    protected static void createBack(Vec3i offset, int x, int y, int z, TextureRegion region, FloatArray vertices) {
//        vertices.add(offset.x + x);
//        vertices.add(offset.y + y);
//        vertices.add(offset.z + z + 1);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(-1);
//        vertices.add(region.getU2());
//        vertices.add(region.getV2());
//
//        vertices.add(offset.x + x);
//        vertices.add(offset.y + y + 1);
//        vertices.add(offset.z + z + 1);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(-1);
//        vertices.add(region.getU2());
//        vertices.add(region.getV());
//
//        vertices.add(offset.x + x + 1);
//        vertices.add(offset.y + y + 1);
//        vertices.add(offset.z + z + 1);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(-1);
//        vertices.add(region.getU());
//        vertices.add(region.getV());
//
//        vertices.add(offset.x + x + 1);
//        vertices.add(offset.y + y);
//        vertices.add(offset.z + z + 1);
//        vertices.add(0);
//        vertices.add(0);
//        vertices.add(-1);
//        vertices.add(region.getU());
//        vertices.add(region.getV2());
//    }

    private Block getB(Chunk chunk, int x, int y, int z) {
        return chunk.get(x, y, z);
    }
}

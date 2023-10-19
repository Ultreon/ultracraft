package com.ultreon.craft.render.world;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FlushablePool;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static com.ultreon.craft.world.World.CHUNK_HEIGHT;
import static com.ultreon.craft.world.World.CHUNK_SIZE;

public final class WorldRenderer implements RenderableProvider {
    static long vertexCount;
    private static long chunkMeshFrees;
    private final ChunkMeshBuilder meshBuilder;
    private final Material material;
    private final Material transparentMaterial;
    private final ScheduledExecutorService chunkScheduler;
    private int visibleChunks;
    private int loadedChunks;

    private final World world;
    private final UltreonCraft game = UltreonCraft.get();

    private static long poolFree;
    private static int poolPeak;
    private static int poolMax;
    private final FlushablePool<ChunkMesh> pool = new FlushablePool<>() {
        @Override
        protected ChunkMesh newObject() {
            return new ChunkMesh();
        }
    };
    private Renderable cursor;
    private ShaderProgram shader;
    private ModelBatch modelBatch;
    private boolean disposed;

    public WorldRenderer(World world, ModelBatch modelBatch) {
        this.world = world;
        this.modelBatch = modelBatch;

        int len = CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE * 6 * 6;

        short[] indices = new short[len];
        short j = 0;

        for (int i = 0; i < len; i += 6, j += 4) {
            indices[i] = (short)(j + 0);
            indices[i + 1] = (short)(j + 1);
            indices[i + 2] = (short)(j + 2);
            indices[i + 3] = (short)(j + 0);
            indices[i + 4] = (short)(j + 2);
            indices[i + 5] = (short)(j + 3);
        }

        Texture texture = this.game.blocksTextureAtlas.getTexture();
        this.material = new Material();
        this.material.set(TextureAttribute.createDiffuse(texture));
        this.material.set(new DepthTestAttribute(GL20.GL_DEPTH_FUNC));
        this.transparentMaterial = new Material();
        this.transparentMaterial.set(TextureAttribute.createDiffuse(texture));
        this.transparentMaterial.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        this.transparentMaterial.set(new DepthTestAttribute(GL20.GL_DEPTH_FUNC));
        this.meshBuilder = new ChunkMeshBuilder(indices);

        this.chunkScheduler = Executors.newSingleThreadScheduledExecutor();
        this.chunkScheduler.scheduleAtFixedRate(() -> {
            Player player = this.game.player;
            if (player != null) {
                world.refreshChunks(player);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public static long getChunkMeshFrees() {
        return WorldRenderer.chunkMeshFrees;
    }

    public static long getVertexCount() {
        return WorldRenderer.vertexCount;
    }

    public void free(Chunk chunk) {
        if (!UltreonCraft.isOnRenderingThread()) {
            UltreonCraft.invoke(() -> this.free(chunk));
            return;
        }
        this.pool.free(chunk.mesh);
        this.pool.free(chunk.transparentMesh);
        chunk.mesh = null;
        WorldRenderer.chunkMeshFrees++;
    }

    @Override
    public void getRenderables(final Array<Renderable> output, final Pool<Renderable> ignored) {
        var player = this.game.player;
        if (player == null) return;

        output.clear();

        var chunks = WorldRenderer.sortChunks(this.world.getLoadedChunks(), player);
        this.loadedChunks = chunks.size();
        this.visibleChunks = 0;

        for (var chunk : chunks) {
            if (!chunk.isReady()) continue;

            Vec3i chunkOffset = chunk.getOffset();
            Vec3f renderOffsetC = chunkOffset.d().sub(player.getPosition().add(0, player.getEyeHeight(), 0)).f();
            chunk.renderOffset.set(renderOffsetC.x, renderOffsetC.y, renderOffsetC.z);

            if (chunk.dirty && chunk.mesh != null) {
                this.free(chunk);
            }

            chunk.dirty = false;

            if (chunk.mesh == null)
                chunk.mesh = this.meshBuilder.buildMesh(this.pool.obtain(), chunk);

            if (chunk.transparentMesh == null)
                chunk.transparentMesh = this.meshBuilder.buildTransparentMesh(this.pool.obtain(), chunk);

            chunk.mesh.chunk = chunk;
            chunk.mesh.renderable.material = this.material;
            chunk.mesh.transform.setToTranslation(chunk.renderOffset);

            chunk.transparentMesh.chunk = chunk;
            chunk.transparentMesh.renderable.material = this.transparentMaterial;
            chunk.transparentMesh.transform.setToTranslation(chunk.renderOffset);

            output.add(chunk.mesh.renderable);
            output.add(chunk.transparentMesh.renderable);

            //noinspection CommentedOutCode
            {
                //* Use in case of memory leak.
//                try {
//                    Thread.sleep(10); //* Set value depending on severity of memory leak.
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
            }
            this.visibleChunks++;

            this.doPoolStatistics();
        }

        HitResult gameCursor = this.game.cursor;
        if (this.cursor == null) {
//            MeshBuilder build = new MeshBuilder();
//            build.begin(new VertexAttributes(VertexAttribute.Position()), GL_TRIANGLES);
//            BoundingBox boundingBox = Blocks.STONE.getBoundingBox(0, 0, 0);
//            BoundingBox boundingBox1 = new BoundingBox();
//            double v = 0.001;
//            boundingBox1.set(boundingBox);
//            boundingBox1.min.sub(-v, v, -v);
//            boundingBox1.max.add(-v, v, -v);
//            BoxShapeBuilder.build(build, boundingBox1.toGdx());
//            boundingBox1.set(boundingBox);
//            boundingBox1.min.sub(-v, -v, v);
//            boundingBox1.max.add(-v, -v, v);
//            BoxShapeBuilder.build(build, boundingBox1.toGdx());
//            boundingBox1.set(boundingBox);
//            boundingBox1.min.sub(v, -v, -v);
//            boundingBox1.max.add(v, -v, -v);
//            BoxShapeBuilder.build(build, boundingBox1.toGdx());
//            Mesh mesh = build.end();
            Mesh mesh = WorldRenderer.buildOutlineBox(0.007f, Color.BLACK);

            int numIndices = mesh.getNumIndices();
            int numVertices = mesh.getNumVertices();
            Renderable renderable = new Renderable();
            renderable.meshPart.mesh = mesh;
            renderable.meshPart.size = numIndices > 0 ? numIndices : numVertices;
            renderable.meshPart.offset = 0;
            renderable.meshPart.primitiveType = GL_TRIANGLES;
            Material material = new Material();
            material.set(ColorAttribute.createDiffuse(0, 0, 0, 1f));
            material.set(new BlendingAttribute());
            material.set(new DepthTestAttribute(false));
            renderable.material = material;
            this.cursor = renderable;
        }
        if (gameCursor != null && gameCursor.collide) {
            Renderable renderable = this.cursor;
            Vec3i pos = gameCursor.pos;
            Vec3f renderOffsetC = pos.d().sub(player.getPosition().add(0, player.getEyeHeight(), 0)).f();
            Vector3 renderOffset = new Vector3(renderOffsetC.x, renderOffsetC.y, renderOffsetC.z);

            renderable.worldTransform.setToTranslation(renderOffset);
            output.add(this.cursor);
        }
    }
    private static Mesh buildOutlineBox(float thickness, Color color) {
        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.begin(new VertexAttributes(VertexAttribute.Position()), GL_TRIANGLES);

        // Top face
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, -thickness, -thickness), new Vector3(1 + thickness, thickness, thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, 1 + -thickness, -thickness), new Vector3(1 + thickness, 1 + thickness, thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, -thickness, 1 + -thickness), new Vector3(1 + thickness, thickness, 1 + thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, 1 + -thickness, 1 + -thickness), new Vector3(1 + thickness, 1 + thickness, 1 +thickness)));

        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, -thickness, -thickness),               new Vector3(thickness, 1 + thickness, thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(1 + -thickness, -thickness, -thickness),        new Vector3(1 + thickness, 1 + thickness, thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(1 + -thickness, -thickness, 1 + -thickness), new Vector3(1 + thickness, 1 + thickness, 1 + thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, -thickness, 1 + -thickness),        new Vector3(thickness, 1 + thickness, 1 + thickness)));

        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, -thickness, -thickness), new Vector3(thickness, thickness, 1 + thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(1 + -thickness, -thickness, -thickness), new Vector3(1 + thickness, thickness, 1 + thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(1 + -thickness, 1 + -thickness, -thickness), new Vector3(1 + thickness, 1 + thickness, 1 + thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, 1 + -thickness, -thickness), new Vector3(thickness, 1 + thickness, 1 + thickness)));

        // Create the mesh from the mesh builder
        return meshBuilder.end();
    }

    private void edge(MeshBuilder builder, Vector3[] vertices, float thickness) {
        Vector3 max = new Vector3();
        Vector3 min = new Vector3();
        for (Vector3 vertex : vertices) {
            max.set(vertex.x + thickness, vertex.y + thickness, vertex.z + thickness);
            min.set(vertex.x - thickness, vertex.y -thickness, vertex.z - thickness);
            BoxShapeBuilder.build(builder, new BoundingBox(min, max));
        }
    }

    private void doPoolStatistics() {
        WorldRenderer.poolFree = this.pool.getFree();
        WorldRenderer.poolPeak = this.pool.peak;
        WorldRenderer.poolMax = this.pool.max;
    }

    @NotNull
    private static List<Chunk> sortChunks(Collection<Chunk> chunks, Player player) {
        List<Chunk> toSort = new ArrayList<>(chunks);
        toSort.sort((o1, o2) -> {
            Vec3d mid1 = new Vec3d(o1.getOffset().x + (float) CHUNK_SIZE, o1.getOffset().y + (float) CHUNK_HEIGHT, o1.getOffset().z + (float) CHUNK_SIZE);
            Vec3d mid2 = new Vec3d(o2.getOffset().x + (float) CHUNK_SIZE, o2.getOffset().y + (float) CHUNK_HEIGHT, o2.getOffset().z + (float) CHUNK_SIZE);
            return Double.compare(mid2.dst(player.getPosition()), mid1.dst(player.getPosition()));
        });
        return toSort;
    }

    public static Matrix4 rotateTowards(Matrix4 transformMatrix, Vector3 currentPos, Vector3 targetPos) {
        // Calculate the direction vector from current to target position
        Vector3 direction = targetPos.cpy().sub(currentPos).nor();

        // Calculate the rotation angle in radians using the direction vector
        float angle = (float) Math.atan2(-direction.z, -direction.x);

        // Create a rotation matrix using LibGDX's Matrix4 API
        transformMatrix.rotateRad(Vector3.Y, angle);
        return transformMatrix;
    }

    public int getVisibleChunks() {
        return this.visibleChunks;
    }

    public int getLoadedChunks() {
        return this.loadedChunks;
    }

    public static long getPoolFree() {
        return WorldRenderer.poolFree;
    }

    public static int getPoolPeak() {
        return WorldRenderer.poolPeak;
    }

    public static int getPoolMax() {
        return WorldRenderer.poolMax;
    }

    public World getWorld() {
        return this.world;
    }

    public void dispose() {
        if (this.disposed) return;
        this.disposed = true;
        this.pool.clear();
        this.pool.flush();
        Renderable cursor1 = this.cursor;
        if (cursor1 != null) {
            Mesh mesh = cursor1.meshPart.mesh;
            if (mesh != null) {
                mesh.dispose();
            }
        }
    }

    public void setShader(ShaderProgram shader) {
        this.shader = shader;
    }

    public boolean isDisposed() {
        return disposed;
    }
}

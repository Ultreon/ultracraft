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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FlushablePool;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.util.BoundingBox;
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

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static com.ultreon.craft.world.World.CHUNK_HEIGHT;
import static com.ultreon.craft.world.World.CHUNK_SIZE;

public final class WorldRenderer implements RenderableProvider {
    static long vertexCount;
    private static long chunkMeshFrees;
    private final ChunkMeshBuilder meshBuilder;
    private final Material material;
    private final Material transparentMaterial;
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

    public WorldRenderer(World world, ModelBatch modelBatch) {
        this.world = world;
        this.modelBatch = modelBatch;

        int len = 49152;

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
        this.pool.free(chunk.trasparentMesh);
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

            if (chunk.trasparentMesh == null)
                chunk.trasparentMesh = this.meshBuilder.buildTransparentMesh(this.pool.obtain(), chunk);

            chunk.mesh.chunk = chunk;
            chunk.mesh.renderable.material = this.material;
            chunk.mesh.transform.setToTranslation(chunk.renderOffset);

            chunk.trasparentMesh.chunk = chunk;
            chunk.trasparentMesh.renderable.material = this.transparentMaterial;
            chunk.trasparentMesh.transform.setToTranslation(chunk.renderOffset);

            output.add(chunk.mesh.renderable);
            output.add(chunk.trasparentMesh.renderable);

            this.visibleChunks++;

            this.doPoolStatistics();
        }

        HitResult gameCursor = this.game.cursor;
        if (this.cursor == null) {
            MeshBuilder build = new MeshBuilder();
            build.begin(new VertexAttributes(VertexAttribute.Position()), GL_TRIANGLES);
            BoundingBox boundingBox = Blocks.STONE.getBoundingBox(0, 0, 0);
            boundingBox.min.sub(0.0625f, 0.0625f, 0.0625f);
            boundingBox.max.add(0.0625f, 0.0625f, 0.0625f);
            BoxShapeBuilder.build(build, boundingBox.toGdx());
            Mesh mesh = build.end();
            int numIndices = mesh.getNumIndices();
            int numVertices = mesh.getNumVertices();
            Renderable renderable = new Renderable();
            renderable.meshPart.mesh = mesh;
            renderable.meshPart.size = numIndices > 0 ? numIndices : numVertices;
            renderable.meshPart.offset = 0;
            renderable.meshPart.primitiveType = GL_TRIANGLES;
            Material material = new Material();
            material.set(ColorAttribute.createDiffuse(1, 1, 1, 0.4f));
            material.set(new BlendingAttribute());
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
}

package com.ultreon.craft.render.world;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FlushablePool;
import com.badlogic.gdx.utils.Pool;
import com.google.common.base.Preconditions;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.collection.PaletteContainer;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static com.ultreon.craft.world.World.CHUNK_HEIGHT;
import static com.ultreon.craft.world.World.CHUNK_SIZE;

public final class WorldRenderer implements RenderableProvider {
    static long vertexCount;
    private static long chunkMeshFrees;
    private final ChunkMeshBuilder meshBuilder;
    private final Material material;
    private final Material transparentMaterial;
    private final Texture breakingTex;
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
    private final Vector3 tmp = new Vector3();
    private final Material breakingMaterial;
    private final Array<TextureRegion> breakingTexRegions = new Array<>(new TextureRegion[6]);
    private Array<Mesh> breakingMeshes;

    public WorldRenderer(World world, ModelBatch modelBatch) {
        this.breakingTex = this.game.getTextureManager().getTexture(UltreonCraft.id("textures/break_stages.png"));
        this.breakingMaterial = new Material(UltreonCraft.strId("block_breaking"));
        this.breakingMaterial.set(TextureAttribute.createDiffuse(this.breakingTex));
        this.breakingMaterial.set(new BlendingAttribute(0.8f));
        for (int i = 0; i < 6; i++) {
            TextureRegion textureRegion = new TextureRegion(this.breakingTex, 0, i / 6f, 1, (i + 1) / 6f);
            this.breakingTexRegions.set(i, textureRegion);
        }
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

        // Block outline.
        Mesh mesh = WorldRenderer.buildOutlineBox(0.005f, Color.BLACK);

        int numIndices = mesh.getNumIndices();
        int numVertices = mesh.getNumVertices();
        Renderable renderable = new Renderable();
        renderable.meshPart.mesh = mesh;
        renderable.meshPart.size = numIndices > 0 ? numIndices : numVertices;
        renderable.meshPart.offset = 0;
        renderable.meshPart.primitiveType = GL_TRIANGLES;
        Material material = new Material();
        material.set(ColorAttribute.createDiffuse(0, 0, 0, 1f));
        material.set(new BlendingAttribute(1.0f));
        material.set(new DepthTestAttribute(false));
        renderable.material = material;
        this.cursor = renderable;

        // Breaking animation meshes.
        BoundingBox boundingBox = Blocks.STONE.getBoundingBox(0, 0, 0).toGdx();
        float v = 0.001f;
        boundingBox.set(boundingBox);
        boundingBox.min.sub(v);
        boundingBox.max.add(v);

        this.breakingMeshes = new Array<>();
        for (int i = 0; i < 6; i++) {
            BakedCubeModel bakedCubeModel = new BakedCubeModel(this.breakingTexRegions.get(i));
            this.breakingMeshes.add(bakedCubeModel.getMesh());
        }
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
        chunk.trasparentMesh = null;
        WorldRenderer.chunkMeshFrees++;
    }

    @Override
    public void getRenderables(final Array<Renderable> output, final Pool<Renderable> renderablePool) {
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

            output.add(this.verifyOutput(chunk.mesh.renderable));
            output.add(this.verifyOutput(chunk.trasparentMesh.renderable));

            for (var entry : chunk.getBreaking().entrySet()) {
                Vec3i key = entry.getKey();
                this.tmp.set(chunk.renderOffset);
                this.tmp.add(key.x+1, key.y, key.z);

                Mesh breakingMesh = this.breakingMeshes.get(Math.round(Mth.clamp(entry.getValue() * 5, 0, 5)));
                int numIndices = breakingMesh.getMaxIndices();
                int numVertices = breakingMesh.getMaxVertices();

                Renderable renderable = renderablePool.obtain();
                renderable.meshPart.mesh = breakingMesh;
                renderable.meshPart.size = numIndices > 0 ? numIndices : numVertices;
                renderable.meshPart.primitiveType = GL_TRIANGLES;
                renderable.material = this.breakingMaterial;
                renderable.worldTransform.setToTranslation(this.tmp);

                output.add(this.verifyOutput(renderable));
            }

            this.visibleChunks++;

            this.doPoolStatistics();
        }

        HitResult gameCursor = this.game.cursor;
        if (gameCursor != null && gameCursor.isCollide()) {
            Renderable renderable = this.cursor;
            Vec3i pos = gameCursor.getPos();
            Vec3f renderOffsetC = pos.d().sub(player.getPosition().add(0, player.getEyeHeight(), 0)).f();
            Vector3 renderOffset = new Vector3(renderOffsetC.x, renderOffsetC.y, renderOffsetC.z);

            renderable.worldTransform.setToTranslation(renderOffset);
            output.add(this.verifyOutput(this.cursor));
        }
    }

    private Renderable verifyOutput(Renderable renderable) {
        Preconditions.checkNotNull(renderable.meshPart.mesh, "Mesh of renderable is null");
        Preconditions.checkNotNull(renderable.material, "Material of renderable is null");
        return renderable;
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
        return this.disposed;
    }

    public Texture getBreakingTex() {
        return this.breakingTex;
    }
}

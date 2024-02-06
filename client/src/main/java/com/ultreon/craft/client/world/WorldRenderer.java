package com.ultreon.craft.client.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FlushablePool;
import com.badlogic.gdx.utils.Pool;
import com.google.common.base.Preconditions;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.client.DisposableContainer;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.imgui.ImGuiOverlay;
import com.ultreon.craft.client.model.block.BakedCubeModel;
import com.ultreon.craft.client.model.block.BlockModel;
import com.ultreon.craft.client.model.block.BlockModelRegistry;
import com.ultreon.craft.client.model.entity.renderer.EntityRenderer;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.client.registry.RendererRegistry;
import com.ultreon.craft.crash.CrashCategory;
import com.ultreon.craft.crash.CrashLog;
import com.ultreon.craft.debug.ValueTracker;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.util.ElementID;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;
import static com.ultreon.craft.client.UltracraftClient.crash;
import static com.ultreon.craft.client.UltracraftClient.id;
import static com.ultreon.craft.world.World.*;

public final class WorldRenderer implements DisposableContainer {
    public static final float SCALE = 1;
    private static final Vec3d TMP_3D_A = new Vec3d();
    private static final Vec3d TMp_3D_B = new Vec3d();
    public static final String OUTLINE_CURSOR_ID = CommonConstants.strId("outline_cursor");
    private final Material material;
    private final Material transparentMaterial;
    private final Texture breakingTex;
    private final Mesh sectionBorder;
    private final Material sectionBorderMaterial;
    private final Environment environment;
    private int visibleChunks;
    private int loadedChunks;
    private static final Vector3 CHUNK_DIMENSIONS = new Vector3(CHUNK_SIZE, CHUNK_HEIGHT, CHUNK_SIZE);
    private static final Vector3 HALF_CHUNK_DIMENSIONS = WorldRenderer.CHUNK_DIMENSIONS.cpy().scl(0.5f);

    private final ClientWorld world;
    private final UltracraftClient client = UltracraftClient.get();

    private final FlushablePool<ChunkMesh> pool = new FlushablePool<>() {
        @Override
        protected ChunkMesh newObject() {
            return new ChunkMesh();
        }
    };
    private final Renderable cursor;
    private boolean disposed;
    private final Vector3 tmp = new Vector3();
    private final Vector3 tmp1 = new Vector3();
    private final Material breakingMaterial;
    private final Array<Mesh> breakingMeshes;
    private final Int2ObjectMap<ModelInstance> modelInstances = new Int2ObjectOpenHashMap<>();
    private final List<Disposable> disposables = new ArrayList<>();
    private final MeshBuilder meshBuilder = new MeshBuilder();
    private long lastChunkBuild;

    public WorldRenderer(ClientWorld world) {
        this.world = world;

        Texture blockTex = this.client.blocksTextureAtlas.getTexture();
        Texture emissiveBlockTex = this.client.blocksTextureAtlas.getEmissiveTexture();

        this.material = new Material();
        this.material.set(TextureAttribute.createDiffuse(blockTex));
        this.material.set(TextureAttribute.createEmissive(emissiveBlockTex));
        this.material.set(new DepthTestAttribute(GL20.GL_DEPTH_FUNC));
        this.transparentMaterial = new Material();
        this.transparentMaterial.set(TextureAttribute.createDiffuse(blockTex));
        this.transparentMaterial.set(TextureAttribute.createEmissive(emissiveBlockTex));
//        this.transparentMaterial.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        this.transparentMaterial.set(new DepthTestAttribute(GL20.GL_DEPTH_FUNC));
//        this.transparentMaterial.set(FloatAttribute.createAlphaTest(0.02f));

        // Chunk border outline
        MeshMaterial result = createChunkOutline();
        this.sectionBorderMaterial = result.material();
        this.sectionBorder = result.mesh();

        // Block outline.
        this.cursor = createBlockOutline();

        // Breaking animation meshes.
        this.breakingTex = this.client.getTextureManager().getTexture(id("textures/break_stages.png"));
        this.breakingMaterial = new Material(UltracraftClient.strId("block_breaking"));
        this.breakingMaterial.set(TextureAttribute.createDiffuse(this.breakingTex));
        this.breakingMaterial.set(new BlendingAttribute(0.8f));
        Array<TextureRegion> breakingTexRegions = new Array<>(new TextureRegion[6]);
        for (int i = 0; i < 6; i++) {
            TextureRegion textureRegion = new TextureRegion(this.breakingTex, 0, i / 6f, 1, (i + 1) / 6f);
            breakingTexRegions.set(i, textureRegion);
        }

        var boundingBox = Blocks.STONE.getBoundingBox(0, 0, 0);
        float v = 0.001f;
        boundingBox.set(boundingBox);
        boundingBox.min.sub(v);
        boundingBox.max.add(v);

        this.breakingMeshes = new Array<>();
        for (int i = 0; i < 6; i++) {
            BakedCubeModel bakedCubeModel = this.deferDispose(new BakedCubeModel(new ElementID("break_stage/stub_" + i), breakingTexRegions.get(i)));
            this.breakingMeshes.add(bakedCubeModel.getMesh());
        }

        // Load textures
        Pixmap[] skyboxTextures = new Pixmap[6];
        String sideTex = "textures/cubemap/skybox_side.png";
        skyboxTextures[0] = this.deferDispose(new Pixmap(UltracraftClient.resource(id(sideTex))));
        skyboxTextures[1] = this.deferDispose(new Pixmap(UltracraftClient.resource(id(sideTex))));
        skyboxTextures[2] = this.deferDispose(new Pixmap(UltracraftClient.resource(id("textures/cubemap/skybox_top.png"))));
        skyboxTextures[3] = this.deferDispose(new Pixmap(UltracraftClient.resource(id("textures/cubemap/skybox_bottom.png"))));
        skyboxTextures[4] = this.deferDispose(new Pixmap(UltracraftClient.resource(id(sideTex))));
        skyboxTextures[5] = this.deferDispose(new Pixmap(UltracraftClient.resource(id(sideTex))));

        Cubemap cubemap = this.deferDispose(new Cubemap(skyboxTextures[0], skyboxTextures[1], skyboxTextures[2], skyboxTextures[3], skyboxTextures[4], skyboxTextures[5]));

        UltracraftClient.LOGGER.info("Setting up world environment");

        this.environment = new Environment();
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1, 1, 1, 1f));
        this.environment.set(new ColorAttribute(ColorAttribute.Fog, 0.6F, 0.7F, 1.0F, 1.0F));
        this.environment.set(new ColorAttribute(ColorAttribute.Specular, 1, 1, 1, 1f));
        this.environment.set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap));
    }

    @NotNull
    private MeshMaterial createChunkOutline() {
        Mesh mesh = this.deferDispose(WorldRenderer.buildOutlineBox(1 / 16f, CHUNK_SIZE, CHUNK_HEIGHT, CHUNK_SIZE));

        Material material = new Material();
        material.set(ColorAttribute.createDiffuse(0, 0f, 0f, 0.25f));
        material.set(new BlendingAttribute());
        material.set(new DepthTestAttribute(false));
        return new MeshMaterial(mesh, material);
    }

    private Renderable createBlockOutline() {
        Mesh mesh = this.deferDispose(WorldRenderer.buildOutlineBox(0.005f));

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
        return renderable;
    }

    public Environment getEnvironment() {
        return this.environment;
    }

    public static long getChunkMeshFrees() {
        return ValueTracker.getChunkMeshFrees();
    }

    public static long getVertexCount() {
        return ValueTracker.getVertexCount();
    }

    public void free(ClientChunk chunk) {
        if (!UltracraftClient.isOnMainThread()) {
            UltracraftClient.invoke(() -> this.free(chunk));
            return;
        }

        if (!chunk.initialized) return;

        @Nullable ChunkMesh mesh = chunk.solidMesh;
        @Nullable ChunkMesh transparentMesh = chunk.transparentMesh;
        if (mesh == null && transparentMesh == null) return;
        if (mesh != null) this.pool.free(mesh);
        if (transparentMesh != null) this.pool.free(transparentMesh);
        chunk.solidMesh = null;
        chunk.transparentMesh = null;
        chunk.initialized = false;
        ValueTracker.setChunkMeshFrees(ValueTracker.getChunkMeshFrees() + 1);
    }

    public void removeEntity(int id) {
        this.checkThread();
        this.modelInstances.remove(id);
    }

    private void checkThread() {
        if (!UltracraftClient.isOnMainThread())
            throw new IllegalStateException("Should only be called on the main thread!");
    }

    public void collect(final Array<Renderable> output, final Pool<Renderable> renderablePool) {
        var player = this.client.player;
        if (player == null) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.F7)) { // TODO: DEBUG
            this.pool.flush();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F6)) { // TODO: DEBUG
            renderablePool.clear();
        }

        output.clear();

        var chunks = WorldRenderer.chunksInViewSorted(this.world.getLoadedChunks(), player);
        this.loadedChunks = chunks.size();
        this.visibleChunks = 0;

        var ref = new ChunkRenderRef();

        Array<ChunkPos> positions = new Array<>();
        UltracraftClient.PROFILER.section("chunks", () -> this.collectChunks(output, renderablePool, chunks, positions, player, ref));

        HitResult gameCursor = this.client.cursor;
        if (gameCursor != null && gameCursor.isCollide() && !this.client.hideHud && !player.isSpectator()) {
            UltracraftClient.PROFILER.section("cursor", () -> {
                Vec3i pos = gameCursor.getPos();
                Vec3f renderOffsetC = pos.d().sub(player.getPosition().add(0, player.getEyeHeight(), 0)).f();

                this.cursor.meshPart.id = OUTLINE_CURSOR_ID;
                this.cursor.worldTransform.setToTranslation(renderOffsetC.x, renderOffsetC.y, renderOffsetC.z);
                output.add(verifyOutput(this.cursor));
            });
        }

        UltracraftClient.PROFILER.section("(Local Player)", () -> {
            LocalPlayer localPlayer = this.client.player;
            if (localPlayer == null || (!this.client.isInThirdPerson() && this.client.config.get().accessibility.hideFirstPersonPlayer)) return;

            this.collectEntity(localPlayer, output, renderablePool);
        });

        UltracraftClient.PROFILER.section("players", () -> {
            for (var remotePlayer : this.client.getMultiplayerData().getRemotePlayers()) {
                UltracraftClient.PROFILER.section(remotePlayer.getType().getId() + " (" + remotePlayer.getName() + ")", () -> {
                    // TODO: Implement if needed
                });
            }
        });
    }

    private void collectChunks(Array<Renderable> output, Pool<Renderable> renderablePool, List<ClientChunk> chunks, Array<ChunkPos> positions, LocalPlayer player, ChunkRenderRef ref) {
        for (var chunk : chunks) {
            if (positions.contains(chunk.getPos(), false)) {
                UltracraftClient.LOGGER.warn("Duplicate chunk: " + chunk.getPos());
                continue;
            }

            positions.add(chunk.getPos());

            if (!chunk.isReady()) continue;
            if (chunk.isDisposed()) {
                if (chunk.solidMesh != null || chunk.transparentMesh != null) {
                    this.free(chunk);
                }
                continue;
            }

            Vec3i chunkOffset = chunk.getOffset();
            Vec3f renderOffsetC = chunkOffset.d().sub(player.getPosition().add(0, player.getEyeHeight(), 0)).f().div(WorldRenderer.SCALE);
            chunk.renderOffset.set(renderOffsetC.x, renderOffsetC.y, renderOffsetC.z);
            if (!this.client.camera.frustum.boundsInFrustum(chunk.renderOffset.cpy().add(WorldRenderer.HALF_CHUNK_DIMENSIONS), WorldRenderer.CHUNK_DIMENSIONS)) {
                continue;
            }


            if (chunk.dirty && !ref.chunkRendered && (chunk.solidMesh != null || chunk.transparentMesh != null) || (chunk.solidMesh != null || chunk.transparentMesh != null) && !ref.chunkRendered && chunk.getWorld().isChunkInvalidated(chunk)) {
                this.free(chunk);
                chunk.immediateRebuild = true;
                chunk.getWorld().onChunkUpdated(chunk);
                chunk.dirty = false;
                ref.chunkRendered = true;
            }

            chunk.dirty = false;

            if (chunk.solidMesh == null || chunk.transparentMesh == null) {
                if (!this.shouldBuildChunks() && !chunk.immediateRebuild) continue;
                chunk.whileLocked(() -> {
                    if (chunk.solidMesh == null) {
                        chunk.solidMesh = this.pool.obtain();
                        var mesh = chunk.solidMesh.meshPart.mesh = chunk.mesher.meshVoxels(meshBuilder, block -> block.doesRender() && !block.isTransparent());
                        chunk.solidMesh.meshPart.size = mesh.getNumIndices();
                        chunk.solidMesh.meshPart.offset = 0;
                        chunk.solidMesh.meshPart.primitiveType = GL_TRIANGLES;
                        chunk.solidMesh.renderable.material = this.material;
                        chunk.solidMesh.renderable.userData = chunk;
                    }

                    if (chunk.transparentMesh == null) {
                        chunk.transparentMesh = this.pool.obtain();
                        var mesh = chunk.transparentMesh.meshPart.mesh = chunk.mesher.meshVoxels(meshBuilder, block -> block.doesRender() && block.isTransparent());
                        chunk.transparentMesh.meshPart.size = mesh.getNumIndices();
                        chunk.transparentMesh.meshPart.offset = 0;
                        chunk.transparentMesh.meshPart.primitiveType = GL_TRIANGLES;
                        chunk.transparentMesh.renderable.material = this.transparentMaterial;
                        chunk.transparentMesh.renderable.userData = chunk;
                    }
                    chunk.loadCustomRendered();

                    chunk.dirty = false;
                    chunk.onUpdated();
                    chunk.initialized = true;
                    this.lastChunkBuild = System.currentTimeMillis();
                });
            }

            chunk.solidMesh.chunk = chunk;
            chunk.solidMesh.transform.setToTranslationAndScaling(chunk.renderOffset, new Vector3(1 / WorldRenderer.SCALE, 1 / WorldRenderer.SCALE, 1 / WorldRenderer.SCALE));

            chunk.transparentMesh.chunk = chunk;
            chunk.transparentMesh.transform.setToTranslationAndScaling(chunk.renderOffset, new Vector3(1 / WorldRenderer.SCALE, 1 / WorldRenderer.SCALE, 1 / WorldRenderer.SCALE));

            output.add(verifyOutput(chunk.solidMesh.renderable));
            output.add(verifyOutput(chunk.transparentMesh.renderable));

            for (var entry : chunk.getBreaking().entrySet()) {
                BlockPos key = entry.getKey();
                this.tmp.set(chunk.renderOffset);
                this.tmp.add(key.x() + 1f, key.y(), key.z());

                Mesh breakingMesh = this.breakingMeshes.get(Math.round(Mth.clamp(entry.getValue() * 5, 0, 5)));
                int numIndices = breakingMesh.getMaxIndices();
                int numVertices = breakingMesh.getMaxVertices();

                Renderable renderable = new Renderable();
                renderable.meshPart.mesh = breakingMesh;
                renderable.meshPart.size = numIndices > 0 ? numIndices : numVertices;
                renderable.meshPart.primitiveType = GL_TRIANGLES;
                renderable.material = this.breakingMaterial;
                renderable.worldTransform.setToTranslationAndScaling(this.tmp.add(-1, 1, 1), new Vector3(1.001f, 1.001f, 1.001f).scl(-1 / WorldRenderer.SCALE));

                output.add(verifyOutput(renderable));
            }

            for (var entry : chunk.getCustomRendered().entrySet()) {
                BlockPos key = entry.getKey();
                this.tmp.set(chunk.renderOffset);
                this.tmp.add(key.x(), key.y(), key.z());

                Block value = entry.getValue();
                BlockModel blockModel = BlockModelRegistry.get(value);
                if (blockModel != null) {
                    blockModel.render(this.tmp, output, renderablePool);
                }
            }

            chunk.renderModels(output);

            if (ImGuiOverlay.isChunkSectionBordersShown()) {
                this.tmp.set(chunk.renderOffset);
                Mesh mesh = this.sectionBorder;

                int numIndices = mesh.getNumIndices();
                int numVertices = mesh.getNumVertices();
                Renderable renderable = new Renderable();
                renderable.meshPart.mesh = mesh;
                renderable.meshPart.size = numIndices > 0 ? numIndices : numVertices;
                renderable.meshPart.offset = 0;
                renderable.meshPart.primitiveType = GL_TRIANGLES;
                renderable.material = this.sectionBorderMaterial;
                Vector3 add = this.tmp.add(0, -WORLD_DEPTH, 0);
                renderable.worldTransform.setToTranslationAndScaling(add, this.tmp1.set(1 / WorldRenderer.SCALE, 1 / WorldRenderer.SCALE, 1 / WorldRenderer.SCALE));

                output.add(verifyOutput(renderable));
            }

            this.visibleChunks++;

            this.doPoolStatistics();
        }

        ClientChunk.flushPool();
    }

    private boolean shouldBuildChunks() {
        return this.lastChunkBuild < System.currentTimeMillis() - 100L;
    }

    public void collectEntity(Entity entity, Array<Renderable> output, Pool<Renderable> renderablePool) {
        try {
            ModelInstance instance = this.modelInstances.get(entity.getId());
            //noinspection unchecked
            var renderer = (EntityRenderer<@NotNull Entity>) RendererRegistry.get(entity.getType());
            if (instance == null) {
                instance = renderer.createInstance(entity);
                if (instance == null) {
                    return;
                }
                this.modelInstances.put(entity.getId(), instance);
            }
            renderer.animate(instance, entity);
            renderer.render(instance, output, renderablePool);
        } catch (Exception e) {
            UltracraftClient.LOGGER.error("Failed to render entity " + entity.getId(), e);
            CrashLog crashLog = new CrashLog("Error rendering entity " + entity.getId(), new Exception());
            CrashCategory category = new CrashCategory("Entity", e);
            category.add("Entity ID", entity.getId());
            category.add("Entity Type", entity.getType().getId());
            crashLog.add("Entity", entity);
            crashLog.addCategory(category);
            crash(crashLog);
        }
    }

    public static Renderable verifyOutput(Renderable renderable) {
        Preconditions.checkNotNull(renderable.meshPart.mesh, "Mesh of renderable is null");
        Preconditions.checkNotNull(renderable.material, "Material of renderable is null");
        return renderable;
    }

    public static Mesh buildOutlineBox(float thickness) {
        return WorldRenderer.buildOutlineBox(thickness, 1, 1, 1);
    }

    public static Mesh buildOutlineBox(float thickness, float width, float height, float depth) {
        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.begin(new VertexAttributes(VertexAttribute.Position()), GL_TRIANGLES);

        WorldRenderer.buildOutlineBox(thickness, width, height, depth, meshBuilder);

        // Create the mesh from the mesh builder
        return meshBuilder.end();
    }

    public static void buildOutlineBox(float thickness, float width, float height, float depth, MeshBuilder meshBuilder) {
        // Top face
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, -thickness, -thickness), new Vector3(width + thickness, thickness, thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, height - thickness, -thickness), new Vector3(width + thickness, height + thickness, thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, -thickness, depth - thickness), new Vector3(width + thickness, thickness, depth + thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, height - thickness, depth - thickness), new Vector3(width + thickness, height + thickness, depth + thickness)));

        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, -thickness, -thickness), new Vector3(thickness, height + thickness, thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(width - thickness, -thickness, -thickness), new Vector3(width + thickness, height + thickness, thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(width - thickness, -thickness, depth - thickness), new Vector3(width + thickness, height + thickness, depth + thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, -thickness, depth - thickness), new Vector3(thickness, height + thickness, depth + thickness)));

        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, -thickness, -thickness), new Vector3(thickness, thickness, depth + thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(width - thickness, -thickness, -thickness), new Vector3(width + thickness, thickness, depth + thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(width - thickness, height - thickness, -thickness), new Vector3(width + thickness, depth + thickness, depth + thickness)));
        BoxShapeBuilder.build(meshBuilder, new BoundingBox(new Vector3(-thickness, height - thickness, -thickness), new Vector3(thickness, height + thickness, depth + thickness)));
    }

    private void doPoolStatistics() {
        ValueTracker.setPoolFree(this.pool.getFree());
        ValueTracker.setPoolPeak(this.pool.peak);
        ValueTracker.setPoolMax(this.pool.max);
    }

    @NotNull
    private static List<ClientChunk> chunksInViewSorted(Collection<ClientChunk> chunks, Player player) {
        List<ClientChunk> list = new ArrayList<>(chunks);
        list = list.stream().sorted((o1, o2) -> {
            Vec3d mid1 = WorldRenderer.TMP_3D_A.set(o1.getOffset().x + (float) CHUNK_SIZE, o1.getOffset().y + (float) CHUNK_HEIGHT, o1.getOffset().z + (float) CHUNK_SIZE);
            Vec3d mid2 = WorldRenderer.TMp_3D_B.set(o2.getOffset().x + (float) CHUNK_SIZE, o2.getOffset().y + (float) CHUNK_HEIGHT, o2.getOffset().z + (float) CHUNK_SIZE);
            return Double.compare(mid1.dst(player.getPosition()), mid2.dst(player.getPosition()));
        }).toList();
        return list;
    }

    public int getVisibleChunks() {
        return this.visibleChunks;
    }

    public int getLoadedChunks() {
        return this.loadedChunks;
    }

    public static long getPoolFree() {
        return ValueTracker.getPoolFree();
    }

    public static int getPoolPeak() {
        return ValueTracker.getPoolPeak();
    }

    public static int getPoolMax() {
        return ValueTracker.getPoolMax();
    }

    public World getWorld() {
        return this.world;
    }

    @Override
    public void dispose() {
        this.disposed = true;

        this.pool.flush();
        this.pool.clear();

        this.disposables.forEach(Disposable::dispose);
    }

    public boolean isDisposed() {
        return this.disposed;
    }

    public Texture getBreakingTex() {
        return this.breakingTex;
    }

    public void renderEntities() {

    }

    public Material getMaterial() {
        return this.material;
    }

    public Material getTransparentMaterial() {
        return this.transparentMaterial;
    }

    public <T extends Disposable> T deferDispose(T disposable) {
        Preconditions.checkNotNull(disposable, "Disposable cannot be null");

        if (this.disposables.contains(disposable)) return disposable;
        if (this.disposed) {
            UltracraftClient.LOGGER.warn("World renderer already disposed, immediately disposing {}", disposable.getClass().getName());
            disposable.dispose();
            return disposable;
        }
        this.disposables.add(disposable);
        return disposable;
    }

    private record MeshMaterial(Mesh mesh, Material material) {

    }

    private static class ChunkRenderRef {
        boolean chunkRendered = false;
    }
}

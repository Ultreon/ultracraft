package com.ultreon.craft.client.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
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
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.imgui.ImGuiOverlay;
import com.ultreon.craft.client.model.block.BakedCubeModel;
import com.ultreon.craft.client.model.entity.EntityModel;
import com.ultreon.craft.client.model.entity.renderer.EntityRenderer;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.client.registry.ModelRegistry;
import com.ultreon.craft.client.registry.RendererRegistry;
import com.ultreon.craft.debug.ValueTracker;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.entity.util.EntitySize;
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
import static com.ultreon.craft.client.UltracraftClient.id;
import static com.ultreon.craft.world.World.*;

public final class WorldRenderer implements Disposable {
    public static final float SCALE = 1;
    private static final Vec3d TMP_3D_A = new Vec3d();
    private static final Vec3d TMp_3D_B = new Vec3d();
    private final Material material;
    private final Material transparentMaterial;
    private final Texture breakingTex;
    private final Mesh playerMesh;
    private final Material playerMaterial;
    private final Mesh sectionBorder;
    private final Material sectionBorderMaterial;
    private final Environment environment;
    private final Cubemap cubemap;
    private final ColorAttribute ambientLight;
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
    private final Material breakingMaterial;
    private final Array<Mesh> breakingMeshes;
    private final Int2ObjectMap<ModelInstance> modelInstances = new Int2ObjectOpenHashMap<>();
//    private final Shader outlineShader = new OutlineShader(Color.BLACK);
    private final Texture blockTex;
    private final Texture emissiveBlockTex;

    public WorldRenderer(ClientWorld world) {
        this.world = world;

        int len = 49152;

        short[] indices = new short[len];
        short j = 0;

        for (int i = 0; i < len; i += 6, j += 4) {
            indices[i] = j;
            indices[i + 1] = (short) (j + 1);
            indices[i + 2] = (short) (j + 2);
            indices[i + 3] = j;
            indices[i + 4] = (short) (j + 2);
            indices[i + 5] = (short) (j + 3);
        }

        this.blockTex = this.client.blocksTextureAtlas.getTexture();
        this.emissiveBlockTex = this.client.blocksTextureAtlas.getEmissiveTexture();
        this.material = new Material();
        this.material.set(TextureAttribute.createDiffuse(this.blockTex));
        this.material.set(TextureAttribute.createEmissive(this.emissiveBlockTex));
        this.material.set(new DepthTestAttribute(GL20.GL_DEPTH_FUNC));
        this.transparentMaterial = new Material();
        this.transparentMaterial.set(TextureAttribute.createDiffuse(this.blockTex));
        this.transparentMaterial.set(TextureAttribute.createEmissive(this.emissiveBlockTex));
        this.transparentMaterial.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        this.transparentMaterial.set(new DepthTestAttribute(GL20.GL_DEPTH_FUNC));
        this.transparentMaterial.set(FloatAttribute.createAlphaTest(0.25f));

        // Chunk border outline
        {
            Mesh mesh = WorldRenderer.buildOutlineBox(1 / 16f, CHUNK_SIZE, CHUNK_HEIGHT, CHUNK_SIZE);

            Material material = new Material();
            material.set(ColorAttribute.createDiffuse(0, 0f, 0f, 0.25f));
            material.set(new BlendingAttribute());
            material.set(new DepthTestAttribute(false));
            this.sectionBorderMaterial = material;
            this.sectionBorder = mesh;
        }

        // Block outline.
        {
            Mesh mesh = WorldRenderer.buildOutlineBox(0.005f);

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
        }

        // Simple player model (just bounding box of the player entity colored #808080)
        {
            MeshBuilder builder = new MeshBuilder();
            builder.begin(new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.ColorPacked()), GL_TRIANGLES);

            EntitySize size = EntityTypes.PLAYER.getSize();
            BoxShapeBuilder.build(builder, 0, size.height() / 2, 0, -size.width() * 2, -size.height(), -size.width() * 2);

            this.playerMesh = builder.end();
            this.playerMaterial = new Material(ColorAttribute.createDiffuse(0.5f, 0.5f, 0.5f, 1f));
        }

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
            BakedCubeModel bakedCubeModel = new BakedCubeModel(breakingTexRegions.get(i));
            this.breakingMeshes.add(bakedCubeModel.getMesh());
        }

        // Load textures
        Pixmap[] skyboxTextures = new Pixmap[6];
        String sideTex = "textures/cubemap/skybox_side.png";
        skyboxTextures[0] = new Pixmap(UltracraftClient.resource(id(sideTex)));
        skyboxTextures[1] = new Pixmap(UltracraftClient.resource(id(sideTex)));
        skyboxTextures[2] = new Pixmap(UltracraftClient.resource(id("textures/cubemap/skybox_top.png")));
        skyboxTextures[3] = new Pixmap(UltracraftClient.resource(id("textures/cubemap/skybox_bottom.png")));
        skyboxTextures[4] = new Pixmap(UltracraftClient.resource(id(sideTex)));
        skyboxTextures[5] = new Pixmap(UltracraftClient.resource(id(sideTex)));

        this.cubemap = new Cubemap(skyboxTextures[0], skyboxTextures[1], skyboxTextures[2], skyboxTextures[3], skyboxTextures[4], skyboxTextures[5]);

        UltracraftClient.LOGGER.info("Setting up world environment");

        float v1 = 2f, v2 = 0.125f;
        this.environment = new Environment();
        this.ambientLight = new ColorAttribute(ColorAttribute.AmbientLight, 1, 1, 1, 1f);
        this.environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1, 1, 1, 1f));
        this.environment.set(new ColorAttribute(ColorAttribute.Fog, 0.6F, 0.7F, 1.0F, 1.0F));
        this.environment.set(new ColorAttribute(ColorAttribute.Specular, 1, 1, 1, 1f));
        this.environment.set(new CubemapAttribute(CubemapAttribute.EnvironmentMap, this.cubemap));
//        this.environment.add(new DirectionalLight().set(0.75f / v1, 0.75f / v1, 0.75f / v1, 0.0f, 0, 1.0f));
//        this.environment.add(new DirectionalLight().set(0.75f / v1, 0.75f / v1, 0.75f / v1, 0.0f, 0, -1.0f));
//        this.environment.add(new DirectionalLight().set(0.5f / v1, 0.5f / v1, 0.5f / v1, 1.0f, 0, 0.0f));
//        this.environment.add(new DirectionalLight().set(0.5f / v1, 0.5f / v1, 0.5f / v1, -1.0f, 0, 0.0f));
//        this.environment.add(new DirectionalLight().set(1.0f / v1, 1.0f / v1, 1.0f / v1, 0, -1, 0));
//        this.environment.add(new DirectionalLight().set(0.25f / v1, 0.25f / v1, 0.25f / v1, 0, 1, 0));
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


        @Nullable ChunkMesh mesh = chunk.mesh;
        @Nullable ChunkMesh transparentMesh = chunk.transparentMesh;
        if (mesh == null && transparentMesh == null) return;
        if (mesh != null) this.pool.free(mesh);
        if (transparentMesh != null) this.pool.free(transparentMesh);
        chunk.mesh = null;
        chunk.transparentMesh = null;
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

        output.clear();

        var chunks = WorldRenderer.chunksInViewSorted(this.world.getLoadedChunks(), player);
        this.loadedChunks = chunks.size();
        this.visibleChunks = 0;

        var ref = new ChunkRenderRef();

        Array<ChunkPos> positions = new Array<>();
        UltracraftClient.PROFILER.section("chunks", () -> this.collectChunks(output, renderablePool, chunks, positions, player, ref));

        HitResult gameCursor = this.client.cursor;
        if (gameCursor != null && gameCursor.isCollide()) {
            UltracraftClient.PROFILER.section("cursor", () -> {
                Vec3i pos = gameCursor.getPos();
                Vec3f renderOffsetC = pos.d().sub(player.getPosition().add(0, player.getEyeHeight(), 0)).f();
                Vector3 renderOffset = new Vector3(renderOffsetC.x, renderOffsetC.y, renderOffsetC.z);

                this.cursor.meshPart.id = "ultracraft:outline_cursor";
//                this.cursor.shader = this.outlineShader;

                this.cursor.worldTransform.setToTranslation(renderOffset);
                output.add(this.verifyOutput(this.cursor));
            });
        }

        UltracraftClient.PROFILER.section("(Local Player)", () -> {
            LocalPlayer localPlayer = this.client.player;
            if (localPlayer == null || !this.client.isInThirdPerson()) return;

            this.collectEntity(localPlayer, output, renderablePool);
        });

        UltracraftClient.PROFILER.section("players", () -> {
            for (var remotePlayer : this.client.getMultiplayerData().getRemotePlayers()) {
                UltracraftClient.PROFILER.section(remotePlayer.getType().getId() + " (" + remotePlayer.getName() + ")", () -> {
                    Vec3f renderOffsetCL = remotePlayer.getPosition().sub(player.getPosition().add(0, player.getEyeHeight(), 0)).f(); //* CoreLibs vector.
                    Vector3 renderOffset = new Vector3(renderOffsetCL.x, renderOffsetCL.y, renderOffsetCL.z);

                    Renderable renderable = renderablePool.obtain();
                    renderable.meshPart.mesh = this.playerMesh;
                    renderable.meshPart.size = this.playerMesh.getMaxIndices();
                    renderable.meshPart.offset = 0;
                    renderable.meshPart.primitiveType = GL_TRIANGLES;
                    renderable.worldTransform.setToTranslation(renderOffset);
                    renderable.material = this.playerMaterial;

                    output.add(this.verifyOutput(renderable));
                });
            }
        });
    }

    private void collectChunks(Array<Renderable> output, Pool<Renderable> renderablePool, List<ClientChunk> chunks, Array<ChunkPos> positions, LocalPlayer player, ChunkRenderRef ref) {
        boolean hasRenderedChunk = false;
        for (var chunk : chunks) {
            if (positions.contains(chunk.getPos(), false)) {
                UltracraftClient.LOGGER.warn("Duplicate chunk: " + chunk.getPos());
                continue;
            }
            positions.add(chunk.getPos());

            if (!chunk.isReady()) continue;
            if (chunk.isDisposed()) {
                if (chunk.mesh != null || chunk.transparentMesh != null) {
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


            if (chunk.dirty && !ref.chunkRendered && (chunk.mesh != null || chunk.transparentMesh != null) || (chunk.mesh != null || chunk.transparentMesh != null) && !ref.chunkRendered && chunk.getWorld().isChunkInvalidated(chunk)) {
                this.free(chunk);
                chunk.getWorld().onChunkUpdated(chunk);
                ref.chunkRendered = true;
            }

            chunk.dirty = false;

            if (!hasRenderedChunk && (Gdx.graphics.getFrameId() / 2) % 20 == 0) {
                if (chunk.mesh == null) {
                    chunk.mesh = this.pool.obtain();
                    var mesh = chunk.mesh.meshPart.mesh = chunk.mesher.meshVoxels(new MeshBuilder(), block -> block.doesRender() && !block.isTransparent());
                    chunk.mesh.meshPart.size = mesh.getNumIndices();
                    chunk.mesh.meshPart.offset = 0;
                    chunk.mesh.meshPart.primitiveType = GL_TRIANGLES;
                    chunk.mesh.renderable.material = this.material;
                    chunk.mesh.renderable.userData = chunk;

                    hasRenderedChunk = true;
                }

                if (chunk.transparentMesh == null) {
                    chunk.transparentMesh = this.pool.obtain();
                    var mesh = chunk.transparentMesh.meshPart.mesh = chunk.mesher.meshVoxels(new MeshBuilder(), block -> block.doesRender() && block.isTransparent());
                    chunk.transparentMesh.meshPart.size = mesh.getNumIndices();
                    chunk.transparentMesh.meshPart.offset = 0;
                    chunk.transparentMesh.meshPart.primitiveType = GL_TRIANGLES;
                    chunk.transparentMesh.renderable.material = this.transparentMaterial;
                    chunk.mesh.renderable.userData = chunk;
                    hasRenderedChunk = true;
                }
            } else if (chunk.mesh == null || chunk.transparentMesh == null) {
                continue;
            }

            chunk.mesh.chunk = chunk;
            chunk.mesh.transform.setToTranslationAndScaling(chunk.renderOffset, new Vector3(1 / WorldRenderer.SCALE, 1 / WorldRenderer.SCALE, 1 / WorldRenderer.SCALE));

            chunk.transparentMesh.chunk = chunk;
            chunk.transparentMesh.transform.setToTranslationAndScaling(chunk.renderOffset, new Vector3(1 / WorldRenderer.SCALE, 1 / WorldRenderer.SCALE, 1 / WorldRenderer.SCALE));

            output.add(this.verifyOutput(chunk.mesh.renderable));
            output.add(this.verifyOutput(chunk.transparentMesh.renderable));

            for (var entry : chunk.getBreaking().entrySet()) {
                BlockPos key = entry.getKey();
                this.tmp.set(chunk.renderOffset);
                this.tmp.add(key.x() + 1, key.y(), key.z());

                Mesh breakingMesh = this.breakingMeshes.get(Math.round(Mth.clamp(entry.getValue() * 5, 0, 5)));
                int numIndices = breakingMesh.getMaxIndices();
                int numVertices = breakingMesh.getMaxVertices();

                Renderable renderable = renderablePool.obtain();
                renderable.meshPart.mesh = breakingMesh;
                renderable.meshPart.size = numIndices > 0 ? numIndices : numVertices;
                renderable.meshPart.primitiveType = GL_TRIANGLES;
                renderable.material = this.breakingMaterial;
                renderable.worldTransform.setToTranslationAndScaling(this.tmp.add(-1, 1, 1), new Vector3(1.001f, 1.001f, 1.001f).scl(-1 / WorldRenderer.SCALE));

                output.add(this.verifyOutput(renderable));
            }

            if (ImGuiOverlay.isChunkSectionBordersShown()) {
                this.tmp.set(chunk.renderOffset);
                Mesh mesh = this.sectionBorder;

                int numIndices = mesh.getNumIndices();
                int numVertices = mesh.getNumVertices();
                Renderable renderable = renderablePool.obtain();
                renderable.meshPart.mesh = mesh;
                renderable.meshPart.size = numIndices > 0 ? numIndices : numVertices;
                renderable.meshPart.offset = 0;
                renderable.meshPart.primitiveType = GL_TRIANGLES;
                renderable.material = this.sectionBorderMaterial;
                Vector3 add = this.tmp.add(0, -WORLD_DEPTH, 0);
                renderable.worldTransform.setToTranslationAndScaling(add, new Vector3(1 / WorldRenderer.SCALE, 1 / WorldRenderer.SCALE, 1 / WorldRenderer.SCALE));

                output.add(this.verifyOutput(renderable));
            }

            this.visibleChunks++;

            this.doPoolStatistics();
        }
    }

    public void collectEntity(Entity entity, Array<Renderable> output, Pool<Renderable> renderablePool) {
        ModelInstance instance = this.modelInstances.get(entity.getId());
        //noinspection unchecked
        var renderer = (EntityRenderer<EntityModel<?>, @NotNull Entity>) RendererRegistry.get(entity.getType());
        if (instance == null) {
            Model model = ModelRegistry.getFinished(entity.getType());
            instance = renderer.createInstance(model);
            this.modelInstances.put(entity.getId(), instance);
        }
        renderer.animate(instance, entity);
        renderer.render(instance, output, renderablePool);
    }

    private Renderable verifyOutput(Renderable renderable) {
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
        this.pool.clear();
        this.pool.flush();
        this.cubemap.dispose();
        if (this.sectionBorder != null) this.sectionBorder.dispose();
        if (this.playerMesh != null) this.playerMesh.dispose();
        if (this.breakingMeshes != null) this.breakingMeshes.forEach(Mesh::dispose);
        Renderable cursor1 = this.cursor;
        if (cursor1 != null) {
            Mesh mesh = cursor1.meshPart.mesh;
            if (mesh != null) {
                mesh.dispose();
                cursor1.meshPart.mesh = null;
            }
        }
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

    private static class ChunkRenderRef {
        boolean chunkRendered = false;
    }
}

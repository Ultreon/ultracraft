package com.ultreon.craft.client.world;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.events.ClientChunkEvents;
import com.ultreon.craft.client.render.meshing.GreedyMesher;
import com.ultreon.craft.collection.PaletteStorage;
import com.ultreon.craft.util.InvalidThreadException;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.ChunkSection;

import java.util.List;

public final class ClientChunk extends Chunk {
    final GreedyMesher mesher;
    private final ClientWorld clientWorld;
    public Vector3 renderOffset = new Vector3();
    public ChunkMesh mesh;
    public ChunkMesh transparentMesh;
    public volatile boolean dirty;
    private List<GreedyMesher.Face> solidFaces;
    private List<GreedyMesher.Face> transparentFaces;
    private MeshBuilder meshBuilder;
    private ModelBuilder modelBuilder;
    private Model solidModel, transparentModel;
    ModelInstance solidModelInst;
    ModelInstance transparentModelInst;
    private final UltracraftClient client = UltracraftClient.get();

    public ClientChunk(ClientWorld world, ChunkPos pos, ChunkSection[] sections, PaletteStorage<Biome> biomes) {
        super(world, pos, sections, biomes);
        this.clientWorld = world;
        this.active = false;

        this.mesher = new GreedyMesher(this, false);
    }

    public void rebuildModels() {
        if (this.solidFaces != null) {
            this.rebuildSolidModel();
        }
        if (this.transparentFaces != null) {
            this.rebuildTransparentModel();
        }
    }

    private void rebuildSolidModel() {
        if (this.solidModel != null) {
            this.solidModel.dispose();
        }

        WorldRenderer worldRenderer = this.client.worldRenderer;
        if (worldRenderer == null) return;

        Mesh solidMesh = this.mesher.meshFaces(this.solidFaces, this.meshBuilder);
        this.modelBuilder.begin();
        this.modelBuilder.part("c-%s".formatted(this.getPos()), solidMesh, GL20.GL_TRIANGLES, worldRenderer.getMaterial());
        this.solidModel = this.modelBuilder.end();

        this.solidModelInst = new ModelInstance(this.solidModel);

        this.solidFaces = null;
    }

    private void rebuildTransparentModel() {
        if (this.transparentModel != null) {
            this.transparentModel.dispose();
        }

        WorldRenderer worldRenderer = this.client.worldRenderer;
        if (worldRenderer == null) return;

        Mesh transparentMesh = this.mesher.meshFaces(this.transparentFaces, this.meshBuilder);
        this.modelBuilder.begin();
        this.modelBuilder.part("c-%s".formatted(this.getPos()), transparentMesh, GL20.GL_TRIANGLES, worldRenderer.getTransparentMaterial());
        this.transparentModel = this.modelBuilder.end();

        this.transparentModelInst = new ModelInstance(this.transparentModel);

        this.transparentFaces = null;
    }

    private void rebuildFaces() {
        this.solidFaces = this.mesher.getFaces(block -> !block.isTransparent());
        this.transparentFaces = this.mesher.getFaces(Block::isTransparent);
        this.active = true;
        ClientChunkEvents.REBUILT.factory().onClientChunkRebuilt(this);
    }

    public void rebuild() {
        this.rebuildFaces();
        this.rebuildSolidModel();
        this.rebuildTransparentModel();
    }

    @Override
    public void dispose() {
        if (!UltracraftClient.isOnMainThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }
        super.dispose();

        WorldRenderer worldRenderer = UltracraftClient.get().worldRenderer;
        if ((this.mesh != null || this.transparentMesh != null) && worldRenderer != null) {
            worldRenderer.free(this);
        }
    }

    @Override
    public Block getFast(int x, int y, int z) {
        if (!UltracraftClient.isOnMainThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }

        return super.getFast(x, y, z);
    }

    @Override
    public boolean setFast(int x, int y, int z, Block block) {
        if (!UltracraftClient.isOnMainThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }

        boolean isBlockSet = super.setFast(x, y, z, block);

        this.dirty = true;
        this.clientWorld.updateChunkAndNeighbours(this);
        return isBlockSet;
    }

    public void updated() {
        this.dirty = false;
    }

    @Override
    public void onUpdated() {
        if (!UltracraftClient.isOnMainThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }

        super.onUpdated();
    }

    @Override
    public ClientWorld getWorld() {
        return this.clientWorld;
    }

    void ready() {
        if (!UltracraftClient.isOnMainThread()) {
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
        }
        this.ready = true;
        this.clientWorld.updateChunkAndNeighbours(this);
    }

    public Object getBounds() {
        return null;
    }
}

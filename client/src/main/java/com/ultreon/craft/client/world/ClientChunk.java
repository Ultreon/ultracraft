package com.ultreon.craft.client.world;

import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.render.meshing.GreedyMesher;
import com.ultreon.craft.collection.PaletteStorage;
import com.ultreon.craft.util.InvalidThreadException;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;

public final class ClientChunk extends Chunk {
    final transient GreedyMesher mesher;
    private final ClientWorld clientWorld;
    public Vector3 renderOffset = new Vector3();
    public ChunkMesh mesh;
    public ChunkMesh transparentMesh;
    public boolean dirty;

    public ClientChunk(ClientWorld world, int size, int height, ChunkPos pos, PaletteStorage<Block> storage) {
        super(world, size, height, pos, storage);
        this.clientWorld = world;
        this.active = true;

        this.mesher = new GreedyMesher(this, false);
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

package com.ultreon.craft.client.world;

import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.util.InvalidThreadException;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.libs.commons.v0.vector.Vec3i;

import java.io.*;

public final class ClientChunk extends Chunk {
    private final ClientWorld clientWorld;
    public Vector3 renderOffset = new Vector3();
    public ChunkMesh mesh;
    public ChunkMesh transparentMesh;
    public boolean dirty;

    public ClientChunk(ClientWorld world, int size, int height, ChunkPos pos, byte[] data) throws IOException {
        super(world, size, height, pos);
        this.clientWorld = world;
        this.active = true;
        
        this.deserializeChunk(data);
    }

    @Override
    public void deserializeChunk(byte[] data) throws IOException {
        super.deserializeChunk(data);
    }

    @Override
    public void dispose() {
        super.dispose();

        WorldRenderer worldRenderer = UltracraftClient.get().worldRenderer;
        if ((this.mesh != null || this.transparentMesh != null) && worldRenderer != null) {
            worldRenderer.free(this);
        }
    }

    @Override
    public Block getFast(int x, int y, int z) {
        if (!UltracraftClient.isOnRenderingThread()) {
            return UltracraftClient.invokeAndWait(() -> this.getFast(x, y, z));
        }
        return super.getFast(x, y, z);
    }

    @Override
    public void setFast(int x, int y, int z, Block block) {
        if (!UltracraftClient.isOnRenderingThread()) {
            UltracraftClient.invokeAndWait(() -> this.setFast(x, y, z, block));
        }
        super.setFast(x, y, z, block);

        this.dirty = true;
        this.clientWorld.updateChunkAndNeighbours(this);
    }

    public void setDirty(boolean b) {
        this.dirty = false;
    }

    @Override
    public void onUpdated() {
        super.onUpdated();

        if (!UltracraftClient.isOnRenderingThread()) {
            throw new InvalidThreadException("Should be on rendering thread.");
        }
    }

    @Override
    public ClientWorld getWorld() {
        return this.clientWorld;
    }

    void ready() {
        if (!UltracraftClient.isOnRenderingThread()) {
            throw new InvalidThreadException("Should be on rendering thread.");
        }
//        this.dirty = true;
        this.ready = true;
        this.clientWorld.updateChunkAndNeighbours(this);
    }
}

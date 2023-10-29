package com.ultreon.craft.client.world;

import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.network.packets.C2SChunkStatusPacket;
import com.ultreon.craft.util.InvalidThreadException;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientWorld extends World implements Disposable {
    @NotNull
    private final UltracraftClient client;
    private final Map<ChunkPos, ClientChunk> chunks = new ConcurrentHashMap<>();

    public ClientWorld(@NotNull UltracraftClient client) {
        super();
        this.client = client;
    }

    public int getRenderDistance() {
        return this.client.settings.renderDistance.get();
    }

    @Override
    protected boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkPos pos) {
        return this.chunks.remove(pos) == chunk;
    }

    @Override
    protected void checkThread() {
        if (!UltracraftClient.isOnMainThread())
            throw new InvalidThreadException("Should be on client main thread.");
    }

    @Override
    public @Nullable ClientChunk getChunk(@NotNull ChunkPos pos) {
        if (!UltracraftClient.isOnMainThread()) {
            return UltracraftClient.invokeAndWait(() -> this.getChunk(pos));
        }
        return this.chunks.get(pos);
    }

    @Override
    public @Nullable ClientChunk getChunkAt(@NotNull BlockPos pos) {
        return (ClientChunk) super.getChunkAt(pos);
    }

    @Override
    public @Nullable ClientChunk getChunkAt(int x, int y, int z) {
        return (ClientChunk) super.getChunkAt(x, y, z);
    }

    @Override
    public Collection<ClientChunk> getLoadedChunks() {
        return this.chunks.values();
    }

    @Override
    public boolean isChunkInvalidated(@NotNull Chunk chunk) {
        if (!UltracraftClient.isOnMainThread()) {
            throw new InvalidThreadException("Should be on rendering thread.");
        }

        return super.isChunkInvalidated(chunk);
    }

    @Override
    public void onChunkUpdated(@NotNull Chunk chunk) {
        if (!UltracraftClient.isOnMainThread()) {
            throw new InvalidThreadException("Should be on rendering thread.");
        }

        super.onChunkUpdated(chunk);
    }

    public Map<ChunkPos, ClientChunk> getChunks() {
        return this.chunks;
    }

    public void loadChunk(ChunkPos pos, byte[] data) {
        var chunk = UltracraftClient.invokeAndWait(() -> this.chunks.get(pos));
        if (chunk == null) chunk = this.createChunk(pos, data);
        else throw new IllegalStateException("Chunk already loaded.");

        UltracraftClient.invoke(chunk::ready);

        this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.SUCCESS));
    }

    @NotNull
    private ClientChunk createChunk(ChunkPos pos, byte[] data) {
        ClientChunk chunk;
        try {
            chunk = new ClientChunk(this, World.CHUNK_SIZE, World.CHUNK_HEIGHT, pos, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.chunks.put(pos, chunk);
        return chunk;
    }

    public void tick() {

    }
}

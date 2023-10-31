package com.ultreon.craft.world;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.collection.PaletteStorage;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.InvalidThreadException;
import com.ultreon.data.types.MapType;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;

import static com.ultreon.craft.world.World.CHUNK_HEIGHT;
import static com.ultreon.craft.world.World.CHUNK_SIZE;

@NotThreadSafe
public final class ServerChunk extends Chunk {
    private final ServerWorld world;

    public ServerChunk(ServerWorld world, int size, int height, ChunkPos pos, PaletteStorage<Block> storage) {
        super(world, size, height, pos, storage);
        this.world = world;
    }

    @Override
    public void setFast(int x, int y, int z, Block block) {
        if (!UltracraftServer.isOnServerThread()) {
            throw new InvalidThreadException("Should be on server thread.");
        }
        super.setFast(x, y, z, block);
    }


    public static ServerChunk load(ServerWorld world, ChunkPos pos, MapType chunkData) {
        var storage = new PaletteStorage<Block>(CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE);

        storage.load(chunkData, Chunk::decodeBlock);

        ServerChunk chunk = new ServerChunk(world, CHUNK_SIZE, World.CHUNK_HEIGHT, pos, storage);
        chunk.load(chunkData);
        return chunk;
    }

    public void load(MapType chunkData) {
        MapType extra = chunkData.getMap("Extra");
        if (extra != null) {
            WorldEvents.LOAD_CHUNK.factory().onLoadChunk(this, extra);
        }
    }

    public MapType save() {
        MapType chunkData = new MapType();

        this.storage.save(chunkData);

        MapType extra = new MapType();
        WorldEvents.SAVE_CHUNK.factory().onSaveChunk(this, extra);
        if (!extra.getValue().isEmpty()) {
            chunkData.put("Extra", extra);
        }
        return chunkData;
    }

    @Override
    public ServerWorld getWorld() {
        return this.world;
    }
}

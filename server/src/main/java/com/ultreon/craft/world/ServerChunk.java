package com.ultreon.craft.world;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.collection.FlatStorage;
import com.ultreon.craft.collection.Storage;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.InvalidThreadException;
import com.ultreon.data.types.MapType;

import javax.annotation.concurrent.NotThreadSafe;

import static com.ultreon.craft.world.World.CHUNK_HEIGHT;
import static com.ultreon.craft.world.World.CHUNK_SIZE;

@NotThreadSafe
public final class ServerChunk extends Chunk {
    private final ServerWorld world;

    /**
     * @deprecated Use {@link #ServerChunk(ServerWorld, ChunkPos, Storage, Storage)} instead
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    public ServerChunk(ServerWorld world, int size, int height, ChunkPos pos, Storage<Block> storage, Storage<Biome> biomeStorage) {
        this(world, pos, storage, biomeStorage);
    }

    public ServerChunk(ServerWorld world, ChunkPos pos, Storage<Block> storage, Storage<Biome> biomeStorage) {
        super(world, pos, storage);
        this.world = world;
    }

    @Override
    public boolean setFast(int x, int y, int z, Block block) {
        if (!UltracraftServer.isOnServerThread()) {
            throw new InvalidThreadException("Should be on server thread.");
        }
        return super.setFast(x, y, z, block);
    }


    public static ServerChunk load(ServerWorld world, ChunkPos pos, MapType chunkData) {
        var storage = new FlatStorage<Block>(CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE);
        var biomeStorage = new FlatStorage<Biome>(CHUNK_SIZE * CHUNK_SIZE);

        MapType blockData = chunkData.getMap("Blocks");
        storage.load(blockData, Chunk::loadBlock);

        MapType biomeData = chunkData.getMap("Biomes");
        biomeStorage.load(biomeData, Biome::load);

        ServerChunk chunk = new ServerChunk(world, pos, storage, biomeStorage);
        chunk.load(chunkData);
        return chunk;
    }

    public void load(MapType chunkData) {
        MapType extra = chunkData.getMap("Extra");
        MapType biomeData = chunkData.getMap("Biomes");

        if (biomeData != null) {
            this.biomeStorage.load(biomeData, Biome::load);
        }

        if (extra != null) {
            WorldEvents.LOAD_CHUNK.factory().onLoadChunk(this, extra);
        }
    }

    public MapType save() {
        if (!UltracraftServer.isOnServerThread()) {
            return UltracraftServer.invokeAndWait(this::save);
        }

        MapType data = new MapType();
        MapType chunkData = new MapType();
        MapType biomeData = new MapType();

        this.storage.save(chunkData, Block::save);
        this.biomeStorage.save(biomeData, Biome::save);
        data.put("Biomes", biomeData);
        data.put("Blocks", chunkData);

        MapType extra = new MapType();
        WorldEvents.SAVE_CHUNK.factory().onSaveChunk(this, extra);
        if (!extra.getValue().isEmpty()) {
            data.put("Extra", extra);
        }
        return data;
    }

    @Override
    public ServerWorld getWorld() {
        return this.world;
    }
}

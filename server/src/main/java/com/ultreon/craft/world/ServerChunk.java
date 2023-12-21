package com.ultreon.craft.world;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.collection.PaletteStorage;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.InvalidThreadException;
import com.ultreon.craft.world.gen.biome.Biomes;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;

import javax.annotation.concurrent.NotThreadSafe;

import static com.ultreon.craft.world.World.CHUNK_HEIGHT;
import static com.ultreon.craft.world.World.CHUNK_SIZE;

@NotThreadSafe
public final class ServerChunk extends Chunk {
    private final ServerWorld world;

    public ServerChunk(ServerWorld world, ChunkPos pos, PaletteStorage<Biome> biomes) {
        super(world, pos, biomes);
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
        var storage = new PaletteStorage<>(CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE, Blocks.AIR);
        var biomeStorage = new PaletteStorage<>(CHUNK_SIZE * CHUNK_SIZE, Biomes.PLAINS);

        MapType blockData = chunkData.getMap("Blocks");
        storage.load(blockData, Chunk::decodeBlock);

        MapType biomeData = chunkData.getMap("Biomes");
        if (biomeData == null) ServerChunk.createPlainsBiomeData(biomeStorage);
        else biomeStorage.load(blockData, Biome::load);

        ServerChunk chunk = new ServerChunk(world, pos, biomeStorage);
        chunk.load(chunkData);
        return chunk;
    }

    private static void createPlainsBiomeData(PaletteStorage<Biome> biomes) {
        biomes.fill(Biomes.PLAINS);
    }

    public void load(MapType chunkData) {
        MapType extra = chunkData.getMap("Extra");
        MapType biomeData = chunkData.getMap("Biomes");

        if (biomeData != null) {
            this.biomes.load(biomeData, Biome::load);
        }

        if (extra != null) {
            WorldEvents.LOAD_CHUNK.factory().onLoadChunk(this, extra);
        }
    }

    public MapType save() {
        if (!UltracraftServer.isOnServerThread())
            return UltracraftServer.invokeAndWait(this::save);

        MapType data = new MapType();
        ListType<MapType> sectionData = new ListType<>();
        MapType biomeData = new MapType();

        for (ChunkSection section : this.sections)
            sectionData.add(section.save());

        this.biomes.save(biomeData, Biome::save);
        data.put("Biomes", biomeData);
        data.put("Sections", sectionData);

        MapType extra = new MapType();
        WorldEvents.SAVE_CHUNK.factory().onSaveChunk(this, extra);
        if (!extra.getValue().isEmpty())
            data.put("Extra", extra);

        return data;
    }

    @Override
    public ServerWorld getWorld() {
        return this.world;
    }
}

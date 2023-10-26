package com.ultreon.craft.world;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;

import static com.ultreon.craft.world.World.CHUNK_SIZE;

public final class ServerChunk extends Chunk {
    private final ServerWorld world;

    public ServerChunk(ServerWorld world, int size, int height, ChunkPos pos) {
        super(world, size, height, pos);
        this.world = world;
    }

    @Override
    public void setFast(int x, int y, int z, Block block) {
        if (!UltracraftServer.isOnServerThread()) {
            UltracraftServer.invokeAndWait(() -> this.setFast(x, y, z, block));
            return;
        }
        super.setFast(x, y, z, block);
    }


    public static Chunk load(ServerWorld world, ChunkPos pos, MapType mapType) {
        ServerChunk chunk = new ServerChunk(world, CHUNK_SIZE, World.CHUNK_HEIGHT, pos);
        chunk.load(mapType);
        return chunk;
    }

    public void load(MapType chunkData) {
        ListType<MapType> sectionsData = chunkData.getList("Sections", new ListType<>());
        int y = 0;
        synchronized (this.lock) {
            for (MapType sectionData : sectionsData) {
//                this.sections[y].dispose();
//                this.sections[y] = new Section(new Vec3i(this.offset.x, this.offset.y + y * this.size, this.offset.z), sectionData);
                y++;
            }
        }

        MapType extra = chunkData.getMap("Extra");
        if (extra != null) {
            WorldEvents.LOAD_CHUNK.factory().onLoadChunk(this, extra);
        }
    }

    public MapType save() {
        MapType chunkData = new MapType();
        ListType<MapType> sectionsData = new ListType<>();
        synchronized (this.lock) {
//            for (Section section : this.sections) {
//                sectionsData.add(section.save());
//            }
        }
        chunkData.put("Sections", sectionsData);

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

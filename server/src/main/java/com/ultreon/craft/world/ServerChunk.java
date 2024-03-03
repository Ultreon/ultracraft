package com.ultreon.craft.world;

import com.ultreon.craft.block.entity.BlockEntity;
import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.craft.collection.FlatStorage;
import com.ultreon.craft.collection.Storage;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.InvalidThreadException;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;

import javax.annotation.concurrent.NotThreadSafe;

import static com.ultreon.craft.world.World.CHUNK_HEIGHT;
import static com.ultreon.craft.world.World.CHUNK_SIZE;

@NotThreadSafe
public final class ServerChunk extends Chunk {
    private final ServerWorld world;
    private final ServerWorld.Region region;
    private boolean modified = false;
    private boolean original = true;
    private boolean locked = false;

    public ServerChunk(ServerWorld world, ChunkPos pos, Storage<BlockMetadata> storage, Storage<Biome> biomeStorage, ServerWorld.Region region) {
        super(world, pos, storage);
        this.world = world;
        this.region = region;
    }

    @Override
    public boolean setFast(int x, int y, int z, BlockMetadata block) {
        if (!UltracraftServer.isOnServerThread()) {
            throw new InvalidThreadException("Should be on server thread.");
        }

        if (this.locked) return false;

        Boolean result = this.region.trySet(() -> {
            this.region.markDirty();
            return super.setFast(x, y, z, block);
        }).getValueOr(false);


        if (result) {
            this.modified = true;
            this.original = false;
        }
        return result;
    }

    public static ServerChunk load(ServerWorld world, ChunkPos pos, MapType chunkData, ServerWorld.Region region) {
        var storage = new FlatStorage<BlockMetadata>(CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE);
        var biomeStorage = new FlatStorage<Biome>(CHUNK_SIZE * CHUNK_SIZE);

        MapType blockData = chunkData.getMap("Blocks");
        storage.load(blockData, Chunk::loadBlock);

        MapType biomeData = chunkData.getMap("Biomes");
        biomeStorage.load(biomeData, Biome::load);

        ServerChunk chunk = new ServerChunk(world, pos, storage, biomeStorage, region);
        chunk.load(chunkData);
        return chunk;
    }

    public void load(MapType chunkData) {
        MapType extra = chunkData.getMap("Extra", new MapType());
        this.original = chunkData.getBoolean("original");
        this.heightMap.load(chunkData.getShortArray("HeightMap"));
        this.lightMap.load(chunkData.getByteArray("LightMap"));
        this.modified = false;

        ListType<MapType> blockEntityData = chunkData.getList("BlockEntities");
        for (MapType blockEntityDatum : blockEntityData) {
            BlockPos blockPos = new BlockPos(blockEntityDatum.getInt("x"), blockEntityDatum.getInt("y"), blockEntityDatum.getInt("z"));
            BlockEntity blockEntity = BlockEntity.fullyLoad(world, blockPos, blockEntityDatum);
            this.setBlockEntity(blockPos, blockEntity);
        }

        WorldEvents.LOAD_CHUNK.factory().onLoadChunk(this, extra);
    }

    protected void setBlockEntity(BlockPos blockPos, BlockEntity blockEntity) {
        if (!UltracraftServer.isOnServerThread()) {
            UltracraftServer.invokeAndWait(() -> setBlockEntity(blockPos, blockEntity));
            return;
        }

        if (this.locked) return;

        super.setBlockEntity(blockPos, blockEntity);
    }

    public MapType save() {
        if (!UltracraftServer.isOnServerThread()) {
            return UltracraftServer.invokeAndWait(this::save);
        }

        this.locked = true;

        MapType data = new MapType();
        MapType chunkData = new MapType();
        MapType biomeData = new MapType();

        this.storage.save(chunkData, BlockMetadata::save);
        this.biomeStorage.save(biomeData, Biome::save);
        data.put("Biomes", biomeData);
        data.put("Blocks", chunkData);
        data.putShortArray("HeightMap", this.heightMap.save());
        data.putByteArray("LightMap", this.lightMap.save());
        data.putBoolean("original", this.original);

        MapType extra = new MapType();
        WorldEvents.SAVE_CHUNK.factory().onSaveChunk(this, extra);
        if (!extra.getValue().isEmpty()) {
            data.put("Extra", extra);
        }

        this.modified = false;
        this.locked = false;

        return data;
    }

    @Override
    public ServerWorld getWorld() {
        return this.world;
    }

    public boolean shouldSave() {
        return modified && ready;
    }

    public boolean isOriginal() {
        return original;
    }
}

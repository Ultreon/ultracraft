package com.ultreon.craft.world;

import com.badlogic.gdx.math.MathUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.debug.DebugFlags;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.s2c.S2CBlockSetPacket;
import com.ultreon.craft.server.CommonConstants;
import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.util.InvalidThreadException;
import com.ultreon.craft.util.OverwriteError;
import com.ultreon.craft.util.Task;
import com.ultreon.craft.util.ValidationError;
import com.ultreon.craft.world.gen.BiomeGenerator;
import com.ultreon.craft.world.gen.TerrainGenerator;
import com.ultreon.craft.world.gen.WorldGenInfo;
import com.ultreon.craft.world.gen.layer.*;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class ServerWorld extends World {
    @Nullable
    private TerrainGenerator terrainGen;
    private final WorldStorage storage;
    private final UltracraftServer server;
    private final RegionStorage regionStorage = new RegionStorage();
    @Nullable
    private CompletableFuture<Boolean> saveFuture;
    @Nullable
    private ScheduledFuture<?> saveSchedule;
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();
    private static long chunkUnloads;
    private static long chunkLoads;
    private static long chunkSaves;
    private final Map<ChunkPos, CompletableFuture<ServerChunk>> loadingChunks = new ConcurrentHashMap<>();

    private static final Biome DEFAULT_BIOME = Biome.builder()
            .noise(NoiseSettingsInit.DEFAULT)
            .domainWarping(seed -> new DomainWarping(UltracraftServer.get().disposeOnClose(NoiseSettingsInit.DOMAIN_X.create(seed)), UltracraftServer.get().disposeOnClose(NoiseSettingsInit.DOMAIN_Y.create(seed))))
            .layer(new WaterTerrainLayer(64))
            .layer(new AirTerrainLayer())
            .layer(new SurfaceTerrainLayer())
            .layer(new StoneTerrainLayer())
            .layer(new UndergroundTerrainLayer())
//			.extraLayer(new StonePatchTerrainLayer(NoiseSettingsInit.STONE_PATCH))
            .build();

    static {
        // TODO Use biome registry
        ServerWorld.DEFAULT_BIOME.buildLayers();

    }

    private final Queue<ChunkPos> chunksToLoad = this.createSyncQueue();
    private final Queue<ChunkPos> chunksToUnload = this.createSyncQueue();
    private final Queue<Runnable> tasks = this.createSyncQueue();

    private final BiomeGenerator generator;
    private final Lock chunkLock = new ReentrantLock();

    private int playTime;

    public ServerWorld(UltracraftServer server, WorldStorage storage) {
        super();
        this.server = server;
        this.storage = storage;

        this.generator = ServerWorld.DEFAULT_BIOME.create(this, this.seed);
    }

    public static long getChunkUnloads() {
        return ServerWorld.chunkUnloads;
    }

    public static long getChunkLoads() {
        return ServerWorld.chunkLoads;
    }

    public static long getChunkSaves() {
        return ServerWorld.chunkSaves;
    }

    public int getChunksToLoad() {
        return this.chunksToLoad.size();
    }

    private <T> Queue<T> createSyncQueue() {
        return Queues.synchronizedQueue(Queues.newConcurrentLinkedQueue());
    }

    public int getRenderDistance() {
        return this.server.getRenderDistance();
    }

    @Override
    protected boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkPos pos) {
        this.checkThread();

        if (!chunk.getPos().equals(pos)) {
            throw new ValidationError("Chunk position (" + chunk.getPos() + ") and provided position (" + pos + ") don't match.");
        }
        if (!this.unloadChunk(pos, true))
            World.LOGGER.warn(World.MARKER, "Failed to unload chunk at " + pos);

        WorldEvents.CHUNK_UNLOADED.factory().onChunkUnloaded(this, chunk.getPos(), chunk);
        return true;
    }

    @Override
    public boolean set(int x, int y, int z, @NotNull Block block) {
        boolean isBlockSet = super.set(x, y, z, block);
        if (isBlockSet) {
            this.sendAllTracking(x, y, z, new S2CBlockSetPacket(new BlockPos(x, y, z), block.getId()));
        }
        return isBlockSet;
    }

    public void sendAllTracking(int x, int y, int z, Packet<? extends ClientPacketHandler> packet) {
        for (var player : this.server.getPlayers()) {
            if (player.getWorld() != this) continue;

            if (player.isChunkActive(World.toChunkPos(x, y, z))) {
                player.connection.send(packet);
            }
        }
    }

    @Nullable
    @SuppressWarnings("UnusedReturnValue")
    private Chunk loadChunk(ChunkPos pos) {
        this.checkThread();

        return this.loadChunk(pos.x(), pos.z());
    }

    @Nullable
    public Chunk loadChunk(int x, int z) {
        this.checkThread();

        return this.loadChunk(x, z, false);
    }

    @Nullable
    public ServerChunk loadChunk(int x, int z, boolean overwrite) {
        this.checkThread();

//        if (x != 17 || z != 18) return null;

        var globalPos = new ChunkPos(x, z);
        var localPos = World.toLocalChunkPos(x, z);

        try {
            var oldChunk = this.getChunk(globalPos);
            if (oldChunk != null && !overwrite) {
                return oldChunk;
            }

            var region = this.getOrOpenRegionAt(globalPos);
            var chunk = region.openChunk(localPos, globalPos);
            if (chunk == null) return null;
            if (chunk.active) {
                throw new IllegalStateError("Chunk is already active.");
            }

            WorldEvents.CHUNK_LOADED.factory().onChunkLoaded(this, globalPos, chunk);
            World.chunkLoads++;

            return chunk;
        } catch (Exception e) {
            World.LOGGER.error(World.MARKER, "Failed to load chunk " + globalPos + ":", e);
            throw e;
        }
    }

    @Nullable
    @SuppressWarnings("UnusedReturnValue")
    private Chunk loadChunkNow(ChunkPos pos) {
        this.checkThread();

        return this.loadChunkNow(pos.x(), pos.z());
    }

    @Nullable
    public Chunk loadChunkNow(int x, int z) {
        this.checkThread();

        return this.loadChunkNow(x, z, false);
    }

    @Nullable
    public ServerChunk loadChunkNow(int x, int z, boolean overwrite) {
        this.checkThread();

        var globalPos = new ChunkPos(x, z);
        var localPos = World.toLocalChunkPos(x, z);

        try {
            var oldChunk = this.getChunk(globalPos);
            if (oldChunk != null && !overwrite) {
                return oldChunk;
            }

            var region = this.getOrOpenRegionAt(globalPos);
            var chunk = region.openChunkNow(localPos, globalPos);
            if (chunk == null) {
                World.LOGGER.warn("Failed to load chunk " + globalPos + "!");
                return null;
            }
            if (chunk.active) {
                throw new IllegalStateError("Chunk is already active.");
            }

            WorldEvents.CHUNK_LOADED.factory().onChunkLoaded(this, globalPos, chunk);
            World.chunkLoads++;

            return chunk;
        } catch (Exception e) {
            World.LOGGER.error(World.MARKER, "Failed to load chunk " + globalPos + ":", e);
            throw e;
        }
    }

    @Deprecated
    private WorldGenInfo getWorldGenInfo(Vec3d pos) {
        var needed = World.getChunksAround(this, pos);
        var chunkPositionsToCreate = this.getChunksToLoad(needed, pos);
        var chunkPositionsToRemove = this.getChunksToUnload(needed);

        var data = new WorldGenInfo();
        data.toLoad = chunkPositionsToCreate;
        data.toRemove = chunkPositionsToRemove;
        data.toUpdate = new ArrayList<>();
        return data;

    }

    public void tick() {
        this.playTime++;

        for (var entity : this.entities.values()) {
            entity.tick();
        }

        var poll = this.tasks.poll();
        if (poll != null) poll.run();

        this.pollChunkQueues();
    }

    private void pollChunkQueues() {
        this.chunkLock.lock();
        try {
            this.server.profiler.section("chunkUnloads", () -> {
                var unload = this.chunksToUnload.poll();
                if (unload != null) {
                    var region = this.regionStorage.getRegionAt(unload);
                    if (region != null && region.getActiveChunk(World.toLocalChunkPos(unload)) != null) {
                        this.server.profiler.section("chunk[" + unload + "]", () -> this.unloadChunk(unload));
                    }
                }
            });
        } catch (Throwable t) {
            World.LOGGER.error("Failed to poll chunk task", t);
        }

        try {
            this.server.profiler.section("chunkLoads", () -> {
                var load = this.chunksToLoad.poll();
                if (load != null) {
                    this.server.profiler.section("chunk[" + load.toString() + "]", () -> this.loadChunk(load));
                }
            });
        } catch (Throwable t) {
            World.LOGGER.error("Failed to poll chunk task", t);
        }
        this.chunkLock.unlock();
    }

    /**
     * Loads/unloads chunks requested by the given refresher.
     * Note: Internal API: Do not call when you don't know what you are doing.
     *
     * @param refresher The refresher that requested the chunks.
     */
    @ApiStatus.Internal
    public void doRefresh(ChunkRefresher refresher) {
        if (!refresher.isFrozen()) return;

        for (ChunkPos pos : refresher.toLoad) {
            this.deferLoadChunk(pos);
        }

        for (ChunkPos pos : refresher.toUnload) {
            this.deferUnloadChunk(pos);
        }
    }

    @ApiStatus.Internal
    public void doRefreshNow(ChunkRefresher refresher) {
        for (ChunkPos pos : refresher.toLoad) {
            this.loadChunkNow(pos);
        }

        for (ChunkPos pos : refresher.toUnload) {
            this.deferUnloadChunk(pos);
        }
    }

    private List<ChunkPos> getChunksToLoad(List<ChunkPos> needed, Vec3d pos) {
        return needed.stream()
                .filter(chunkPos -> this.getChunk(chunkPos) == null)
                .sorted(Comparator.comparingDouble(o -> o.getChunkOrigin().dst(pos)))
                .collect(Collectors.toList());
    }

    private List<ChunkPos> getChunksToUnload(List<ChunkPos> needed) {
        List<ChunkPos> toRemove = new ArrayList<>();
        for (var pos : this.getLoadedChunks().stream().map(Chunk::getPos).filter(pos -> {
            var chunk = this.getChunk(pos);
            return chunk != null && !needed.contains(pos);
        }).toList()) {
            if (this.getChunk(pos) != null) {
                toRemove.add(pos);
            }
        }

        return toRemove;
    }

    @Deprecated
    public void refreshChunks(float x, float z) {
        this.refreshChunks(new Vec3d(x, World.WORLD_DEPTH, z));
    }

    @Deprecated
    public void refreshChunks(Vec3d ignoredVec) {

    }

    @Deprecated
    public void refreshChunks(Player ignoredPlayer) {

    }

    private void deferLoadChunk(ChunkPos chunkPos) {
        this.chunkLock.lock();
        try {
            if (this.chunksToLoad.contains(chunkPos)) return;
            this.chunksToLoad.offer(chunkPos);
        } catch (Throwable t) {
            World.LOGGER.error("Failed to defer chunk " + chunkPos + ":", t);
        }
        this.chunkLock.unlock();
    }

    private void deferUnloadChunk(ChunkPos chunkPos) {
        this.chunkLock.lock();
        try {
            if (this.chunksToUnload.contains(chunkPos)) return;
            this.chunksToUnload.add(chunkPos);
        } catch (Throwable t) {
            World.LOGGER.error("Failed to defer chunk " + chunkPos + ":", t);
        }
        this.chunkLock.unlock();
    }

    /**
     * Unloads a chunk from the world.
     *
     * @param chunkPos     the position of the chunk to unload
     * @param save         if true, the chunk will be saved to disk.
     * @param ignoredForce deprecated.
     * @return true if the chunk was successfully unloaded.
     * @deprecated use {@link #unloadChunk(ChunkPos, boolean)} instead.
     */
    @Deprecated
    public boolean unloadChunk(ChunkPos chunkPos, boolean save, @Deprecated boolean ignoredForce) {
        return this.unloadChunk(chunkPos, save);
    }

    /**
     * Unloads a chunk from the world.
     *
     * @param chunkPos the position of the chunk to unload
     * @param save     if true, the chunk will be saved to disk.
     * @return true if the chunk was successfully unloaded.
     */
    public boolean unloadChunk(ChunkPos chunkPos, boolean save) {
        this.checkThread();

        var region = this.regionStorage.getRegionAt(chunkPos);
        if (region == null)
            throw new IllegalStateException("Region is unloaded while unloading chunk " + chunkPos);

        var localChunkPos = World.toLocalChunkPos(chunkPos);

        if (region.getActiveChunk(localChunkPos) == null)
            throw new IllegalStateError("Tried to unload chunk %s but it isn't active".formatted(chunkPos));

        var chunk = region.deactivate(localChunkPos);
        if (chunk == null) {
            throw new IllegalStateError("Tried to unload non-existing chunk: " + chunkPos);
        }

        if (region.isEmpty() && save) {
            try {
                this.saveRegion(region);
            } catch (Exception e) {
                World.LOGGER.warn("Failed to save region %s:".formatted(region.getPos()), e);
            }
        }

        ServerWorld.chunkUnloads++;
        return true;
    }

    public int getPlayTime() {
        return this.playTime;
    }

    @Override
    protected void checkThread() {
        if (!UltracraftServer.isOnServerThread())
            throw new InvalidThreadException("Should be on server thread.");
    }

    @Override
    public @Nullable ServerChunk getChunk(@NotNull ChunkPos pos) {
        var region = this.regionStorage.getRegionAt(pos);
        if (region == null) return null;
        var localPos = World.toLocalChunkPos(pos);
        var chunk = region.getChunk(localPos);
        if (chunk != null) {
            var foundAt = chunk.getPos();
            if (!foundAt.equals(localPos)) {
                throw new ValidationError("Chunk expected to be found at %s was found at %s instead.".formatted(pos, foundAt));
            }
        }
        return chunk;
    }

    /**
     * Get all chunks currently loaded in.
     *
     * @return the currently loaded chunks.
     */
    @Override
    public Collection<ServerChunk> getLoadedChunks() {
        var regions = this.regionStorage.regions.values();
        return regions.stream().flatMap(value -> value.getChunks().stream()).toList();
    }

    /**
     * Disposes the world, and cleans up the objects it uses.
     * Added for clean closing of the world.
     */
    @Override
    @ApiStatus.Internal
    public void dispose() {
        var saveSchedule = this.saveSchedule;
        if (saveSchedule != null) saveSchedule.cancel(true);
        this.saveExecutor.shutdownNow().clear();

        try {
            this.save(false);
        } catch (IOException e) {
            World.LOGGER.warn(World.MARKER, "Saving failed:", e);
        }

        for (var region : this.regionStorage.regions.values()) {
            try {
                this.saveRegion(region, true);
            } catch (Exception e) {
                World.LOGGER.warn("Failed to save region %s:".formatted(region.getPos()), e);
                var remove = this.regionStorage.regions.remove(region.getPos());
                if (remove != region)
                    this.server.crash(new ValidationError("Removed region is not the region that got saved."));
                region.dispose();
            }
        }
        this.generator.dispose();
    }

    @Override
    public BreakResult continueBreaking(@NotNull BlockPos breaking, float amount, @NotNull Player breaker) {
        return super.continueBreaking(breaking, amount, breaker);
    }

    /**
     * Play a sound at a specific position.
     *
     * @param sound sound to play.
     * @param x     the X-position.
     * @param y     the Y-position.
     * @param z     the Z-position.
     */
    @Override
    public void playSound(@NotNull SoundEvent sound, double x, double y, double z) {
        float range = sound.getRange();
        var playersWithinRange = this.getPlayersWithinRange(x, y, z, range);
        for (Player player : playersWithinRange) {
            player.playSound(sound, (float) ((range - player.getPosition().dst(x, y, z)) / range));
        }
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    /**
     * @param x     the origin X-position.
     * @param y     the origin Y-position.
     * @param z     the origin Z-position.
     * @param range the range.
     * @return the players within the range of the XYZ coordinates.
     */
    public List<ServerPlayer> getPlayersWithinRange(double x, double y, double z, float range) {
        var withinRange = new ArrayList<ServerPlayer>();

        // Assuming you have a list of Player objects with their coordinates
        var allPlayers = this.server.getPlayers();

        for (var player : allPlayers) {
            var playerX = player.getX();
            var playerY = player.getY();
            var playerZ = player.getZ();

            // Calculate the distance between the given point (x, y, z) and the player's coordinates
            var distance = Math.sqrt(Math.pow(playerX - x, 2) + Math.pow(playerY - y, 2) + Math.pow(playerZ - z, 2));

            // Check if the distance is within the specified range
            if (distance <= range) {
                withinRange.add(player);
            }
        }

        return withinRange;
    }

    /**
     * @return the storage for world data.
     */
    public WorldStorage getStorage() {
        return this.storage;
    }

    /**
     * Loads the world from the disk.
     *
     * @throws IOException when world loading fails.
     */
    @ApiStatus.Internal
    public void load() throws IOException {
        this.storage.createDir("data/");
        this.storage.createDir("regions/");

        if (this.storage.exists("data/entities.ubo")) {
            ListType<MapType> entityListData = this.storage.read("data/entities.ubo");
            for (var entityData : entityListData) {
                var entity = Entity.loadFrom(this, entityData);
                this.entities.put(entity.getId(), entity);
            }
        }

        WorldEvents.LOAD_WORLD.factory().onLoadWorld(this, this.storage);

        this.saveSchedule = this.server.schedule(new Task<>(new Identifier("auto_save")) {
            @Override
            public void run() {
                try {
                    ServerWorld.this.save(true);
                } catch (Exception e) {
                    World.LOGGER.error(World.MARKER, "Failed to save world:", e);
                }
                ServerWorld.this.saveSchedule = ServerWorld.this.server.schedule(this, CommonConstants.AUTO_SAVE_DELAY, CommonConstants.AUTO_SAVE_DELAY_UNIT);
            }
        }, CommonConstants.INITIAL_AUTO_SAVE_DELAY, CommonConstants.AUTO_SAVE_DELAY_UNIT);
    }

    /**
     * Save the world to disk.
     *
     * @param silent true to silence the logs.
     * @throws IOException when saving the world fails.
     */
    @ApiStatus.Internal
    public void save(boolean silent) throws IOException {
        if (!silent) World.LOGGER.info(World.MARKER, "Saving world: " + this.storage.getDirectory().getFileName());

        var entitiesData = new ListType<MapType>();
        for (var entity : this.entities.values()) {
            if (entity instanceof Player) continue;

            var entityData = entity.save(new MapType());
            entitiesData.add(entityData);
        }
        this.storage.write(entitiesData, "data/entities.ubo");

//		Player player = UltreonCraft.get().player;
//		MapType playerData = player == null ? new MapType() : player.save(new MapType());
//		this.worldStorage.write(playerData, "data/player.ubo");

        for (var region : this.regionStorage.regions.values()) {
            this.saveRegion(region, false);
        }

        WorldEvents.SAVE_WORLD.factory().onSaveWorld(this, this.storage);

        if (!silent) World.LOGGER.info(World.MARKER, "Saved world: " + this.storage.getDirectory().getFileName());
    }

    /**
     * Save a region to disk.
     *
     * @param region the region to save.
     */
    public void saveRegion(Region region) {
        this.saveRegion(region, true);
    }

    /**
     * Save a region to disk.
     *
     * @param region  the region to save.
     * @param dispose true to also dispose the region.
     */
    public void saveRegion(Region region, boolean dispose) {
        if (!UltracraftServer.isOnServerThread()) {
            UltracraftServer.invokeAndWait(() -> this.saveRegion(region, dispose));
        }

        var file = this.storage.regionFile(region.getPos());
        try (var stream = new GZIPOutputStream(new FileOutputStream(file, false))) {
            var dataStream = new DataOutputStream(stream);

            this.regionStorage.save(region, dataStream, dispose);
            stream.finish();
            stream.flush();
        } catch (IOException e) {
            World.LOGGER.error("Failed to save region %s".formatted(region.getPos()), e);
        }
    }

    /**
     * Internal asynchronous save method.
     *
     * @param silent true to silence the logs.
     * @return the future of the save function. Will return true if successful when finished.
     */
    @ApiStatus.Internal
    public CompletableFuture<Boolean> saveAsync(boolean silent) {
        var saveSchedule = this.saveSchedule;
        if (saveSchedule != null && !saveSchedule.isDone()) {
            return this.saveFuture != null ? this.saveFuture : CompletableFuture.completedFuture(true);
        }
        if (saveSchedule != null) {
            saveSchedule.cancel(false);
        }
        try {
            return this.saveFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    this.save(silent);
                    return true;
                } catch (Exception e) {
                    World.LOGGER.error(World.MARKER, "Failed to save world", e);
                    return false;
                }
            }, this.saveExecutor);
        } catch (Exception e) {
            World.LOGGER.error(World.MARKER, "Failed to save world", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    @NotNull
    private Region getOrOpenRegionAt(ChunkPos chunkPos) {
        this.checkThread();

        var rx = chunkPos.x() / World.REGION_SIZE;
        var rz = chunkPos.z() / World.REGION_SIZE;

        var regionPos = new RegionPos(rx, rz);
        var regions = this.regionStorage.regions;
        var oldRegion = regions.get(regionPos);
        if (oldRegion != null) {
            return oldRegion;
        }

        try {
            if (this.storage.regionExists(rx, rz)) {
                return this.openRegion(rx, rz);
            }
        } catch (IOException e) {
            World.LOGGER.error("Region at %s,%s failed to load:".formatted(rx, rz), e);
        }

        var region = new Region(this, regionPos);
        this.regionStorage.regions.put(regionPos, region);

        return region;
    }

    @NotNull
    private Region openRegion(int rx, int rz) throws IOException {
        var fileHandle = this.storage.regionFile(rx, rz);
        try (var stream = new GZIPInputStream(new FileInputStream(fileHandle))) {
            var dataStream = new DataInputStream(stream);
            return this.regionStorage.load(this, dataStream);
        }
    }

    /**
     * Spawn an entity into the world.
     *
     * @param entity the entity to spawn.
     * @param <T>    the entity.
     * @return the spawned entity. (Same as {@code entity} parameter)
     */
    @Override
    public <T extends Entity> T spawn(@NotNull T entity) {
        if (!(entity instanceof ServerPlayer) && entity instanceof Player)
            throw new IllegalStateException("Tried to spawn a non-server player in a server world.");

        return super.spawn(entity);
    }

    /**
     * Prepares spawn for a player.<br>
     * This method is synchronous with the server thread.<br>
     * The action will block the thread.
     *
     * @param player the player to prepare the spawn for.
     */
    public void prepareSpawn(Player player) {
        if (!UltracraftServer.isOnServerThread()) {
            UltracraftServer.invokeAndWait(() -> this.prepareSpawn(player));
            return;
        }

        ChunkPos origin = player.getChunkPos();
        for (int x = origin.x() - 1; x <= origin.x() + 1; x++) {
            for (int z = origin.z() - 1; z <= origin.z() + 1; z++) {
                this.loadChunk(x, z);
            }
        }
    }

    /**
     * @return the server this world belongs to.
     */
    public UltracraftServer getServer() {
        return this.server;
    }

    /**
     * Should only be used for debugging.<br>
     * Note that this method is not thread safe.<br>
     * Only run this method in the server thread.<br>
     * Be sure to remove the client chunk before calling this.
     *
     * @param globalPos the position of the chunk in global space.
     */
    @ApiStatus.Experimental
    public void regenerateChunk(ChunkPos globalPos) {
        this.checkThread();
        this.unloadChunk(globalPos);
        Region region = this.getOrOpenRegionAt(globalPos);
        var localPos = World.toLocalChunkPos(globalPos);
        region.activeChunks.remove(localPos);
        region.chunks.remove(localPos);
        region.generateChunk(localPos, globalPos);
    }

    /**
     * Sets the spawn point for the world.<br>
     * The spawn point is set randomly.
     */
    public void setupSpawn() {
        int spawnChunkX = MathUtils.random(-32, 31);
        int spawnChunkZ = MathUtils.random(-32, 31);
        int spawnX = MathUtils.random(spawnChunkX * 16, spawnChunkX * 16 + 15);
        int spawnZ = MathUtils.random(spawnChunkZ * 16, spawnChunkZ * 16 + 15);

        this.setSpawnPoint(spawnX, spawnZ);
    }

    /**
     * The region class.
     * Note: This class is not thread safe.
     *
     * @author XyperCode
     * @since 0.1.0
     */
    @NotThreadSafe
    public static class Region implements ServerDisposable {
        private final Set<ChunkPos> activeChunks = new CopyOnWriteArraySet<>();
        private final RegionPos pos;
        public int dataVersion;
        public String lastPlayedIn = UltracraftServer.get().getGameVersion();
        private Map<ChunkPos, ServerChunk> chunks = new ConcurrentHashMap<>();
        private boolean disposed = false;
        private final ServerWorld world;
        private final List<ChunkPos> generatingChunks = new CopyOnWriteArrayList<>();
        private final Object buildLock = new Object();

        /**
         * Constructs a new region with the given world and position.
         *
         * @param world the world this region belongs to.
         * @param pos   the position of the region.
         */
        public Region(ServerWorld world, RegionPos pos) {
            this.world = world;
            this.pos = pos;
        }

        /**
         * Constructs a new region with the given world and position. It also preloads all chunks.
         *
         * @param world  the world this region belongs to.
         * @param pos    the position of the region.
         * @param chunks the chunks to load into the region.
         */
        public Region(ServerWorld world, RegionPos pos, Map<ChunkPos, ServerChunk> chunks) {
            this.world = world;
            this.pos = pos;
            this.chunks = chunks;
        }

        /**
         * @return all loaded chunks within the region.
         */
        public Collection<ServerChunk> getChunks() {
            return Collections.unmodifiableCollection(this.chunks.values());
        }

        /**
         * @return the position of the region.
         */
        public RegionPos pos() {
            return this.getPos();
        }

        /**
         * Dispose the region, clearing up chunk data for the garbage collector to free up memory.
         * Note: Internal API.
         * Should only be called if you know what you are doing.
         */
        @ApiStatus.Internal
        public void dispose() {
            this.validateThread();
            if (this.disposed) return;
            this.disposed = true;

            for (Chunk value : this.chunks.values()) {
                value.dispose();
            }

            this.chunks.clear();
        }

        /**
         * Deactivates the chunk at the given position.
         * The chunk will still be loaded in memory for reuse.
         *
         * @param chunkPos the local position of the chunk to deactivate.
         * @return the deactivated chunk, or null if the chunk wasn't loaded.
         */
        @Nullable
        public Chunk deactivate(@NotNull ChunkPos chunkPos) {
            this.validateLocalPos(chunkPos);
            this.validateThread();
            Chunk chunk = this.chunks.get(chunkPos);

            if (chunk == null)
                return null;

            if (!this.activeChunks.remove(chunkPos))
                throw new IllegalStateError("Can't deactivate an already inactive chunk.");

            chunk.active = false;
            return chunk;
        }

        /**
         * Activate the chunk at the given position.
         *
         * @param chunkPos  the local position of the chunk to activate.
         * @param globalPos the global position of the chunk to activate.
         * @return the acivated chunk, or null if the chunk wasn't loaded.
         */
        @Nullable
        @CanIgnoreReturnValue
        public Chunk activate(@NotNull ChunkPos chunkPos, ChunkPos globalPos) {
            this.validateLocalPos(chunkPos);
            this.validateThread();

            @Nullable Chunk chunk = this.chunks.get(chunkPos);

            if (chunk == null)
                return null;

            if (this.activeChunks.contains(chunkPos))
                return chunk;

            this.activeChunks.add(chunkPos);
            chunk.active = true;

            return chunk;
        }

        private void validateLocalPos(ChunkPos chunkPos) {
            Preconditions.checkElementIndex(chunkPos.x(), World.REGION_SIZE, "Chunk x-position out of range");
            Preconditions.checkElementIndex(chunkPos.z(), World.REGION_SIZE, "Chunk z-position out of range");
        }

        /**
         * Get a currently loaded chunk with the specified local position.
         *
         * @param chunkPos the position of the chunk to get.
         * @return the chunk at that position, or null if the chunk wasn't loaded.
         */
        @Nullable
        public ServerChunk getChunk(ChunkPos chunkPos) {
            this.validateLocalPos(chunkPos);
            this.validateThread();
            return this.chunks.get(chunkPos);
        }

        /**
         * Get the currently active chunks in this section.
         *
         * @return the active chunks.
         */
        public Set<ChunkPos> getActiveChunks() {
            return Collections.unmodifiableSet(this.activeChunks);
        }

        /**
         * Get the active chunk at the specified position;
         *
         * @param pos the position of the active chunk to get.
         * @return the active chunk, or null if there's no active chunk at the specified position.
         */
        @Nullable
        public Chunk getActiveChunk(ChunkPos pos) {
            this.validateLocalPos(pos);
            this.validateThread();
            if (!this.activeChunks.contains(pos)) {
                return null;
            }
            return this.chunks.get(pos);
        }

        /**
         * @return true if this region has active chunks.
         */
        public boolean hasActiveChunks() {
            return !this.activeChunks.isEmpty();
        }

        /**
         * @return true if there are no active chunks.
         */
        public boolean isEmpty() {
            return this.activeChunks.isEmpty();
        }

        @CanIgnoreReturnValue
        public Chunk putChunk(@NotNull ChunkPos pos, @NotNull ServerChunk chunk) {
            this.validateLocalPos(pos);
            this.validateThread();

            if (this.chunks.containsKey(pos))
                throw new IllegalStateError("Chunk is already loaded");

            return this.chunks.put(pos, chunk);
        }

        private void validateThread() {
            if (!UltracraftServer.isOnServerThread()) {
                throw new InvalidThreadException("Should be on server thread.");
            }
        }

        /**
         * Generate a chunk at the specified chunk position local to the region.
         *
         * @param pos       the position of the chunk to generate.
         * @param globalPos the global position of the chunk to generate.
         */
        @NewInstance
        public void generateChunk(ChunkPos pos, ChunkPos globalPos) {
            this.validateThread();

            this.buildChunkAsync(globalPos);
        }

        /**
         * Generate a chunk at the specified chunk position local to the region.
         *
         * @param pos       the position of the chunk to generate.
         * @param globalPos the global position of the chunk to generate.
         * @return
         */
        @NewInstance
        public ServerChunk generateChunkNow(ChunkPos pos, ChunkPos globalPos) {
            this.validateThread();

            // Add the chunk to the list of generating chunks.
            // Will return immediately if the chunk is already being built.
            synchronized (this.buildLock) {
                if (this.generatingChunks.contains(globalPos)) return null;
                this.generatingChunks.add(globalPos);
            }

            var ref = new Object() {
                ServerChunk builtChunk = null;
            };
            try {
                ref.builtChunk = this.buildChunk(globalPos);
            } catch (Throwable t) {
                this.generatingChunks.remove(globalPos);
                World.LOGGER.error("Failed to build chunk at %s:".formatted(globalPos), t);
                throw new Error(t);
            }

            var players = this.world.getServer().getPlayersInChunk(globalPos);
            players.forEach(player -> {
                try {
                    BlockPos globalTpPos = player.blockPosition();
                    BlockPos localTpPos = World.toLocalBlockPos(globalTpPos);
                    int x = localTpPos.x();
                    int z = localTpPos.z();
                    player.teleportTo(globalTpPos.x(), ref.builtChunk.ascend(x, (int) player.getY(), z), globalTpPos.z());
                } catch (Exception e) {
                    World.LOGGER.error("Failed to teleport player outside unloaded chunk:", e);
                }
            });

            return ref.builtChunk;
        }

        /**
         * Build the chunk at the specified chunk position local to the region.
         * Note: this method is asynchronous.
         *
         * @param globalPos the global position of the chunk to generate.
         */
        private void buildChunkAsync(ChunkPos globalPos) {
            if (DebugFlags.WARN_CHUNK_BUILD_OVERLOAD && this.world.executor.getActiveCount() == this.world.executor.getMaximumPoolSize()) {
                World.LOGGER.warn("Chunk building is being overloaded!");
            }

            // Add the chunk to the list of generating chunks.
            // Will return immediately if the chunk is already being built.
            synchronized (this.buildLock) {
                if (this.generatingChunks.contains(globalPos)) return;
                this.generatingChunks.add(globalPos);
            }

            // Build the chunk asynchronously.
            CompletableFuture.supplyAsync(() -> {
                try {
                    return this.buildChunk(globalPos);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }, this.world.executor).thenAccept(builtChunk -> {
                UltracraftServer.invoke(() -> {
                    var players = this.world.getServer().getPlayersInChunk(globalPos);
                    players.forEach(player -> {
                        try {
                            BlockPos globalTpPos = player.blockPosition();
                            BlockPos localTpPos = World.toLocalBlockPos(globalTpPos);
                            int x = localTpPos.x();
                            int z = localTpPos.z();
                            player.teleportTo(globalTpPos.x(), builtChunk.ascend(x, (int) player.getY(), z), globalTpPos.z());
                        } catch (Exception e) {
                            World.LOGGER.error("Failed to teleport player outside unloaded chunk:", e);
                        }
                    });
                });
            });
        }

        @CheckReturnValue
        private ServerChunk buildChunk(ChunkPos globalPos) throws Throwable {
            var localPos = World.toLocalChunkPos(globalPos);
            var chunk = new BuilderChunk(this.world, Thread.currentThread(), World.CHUNK_SIZE, World.CHUNK_HEIGHT, localPos);
            // Generate terrain of the chunk in columns.
            for (var bx = 0; bx < World.CHUNK_SIZE; bx++) {
                for (var by = 0; by < World.CHUNK_SIZE; by++) {
                    this.world.generator.processColumn(chunk, bx, by);
                }
            }

            WorldEvents.CHUNK_BUILT.factory().onChunkGenerated(this.world, this, chunk);

            // Put the chunk into the list of loaded chunks.
            ServerChunk builtChunk = chunk.build();
            this.chunks.put(localPos, builtChunk);

            // Send the chunk to all connections.
            try {
                this.world.server.sendChunk(globalPos, builtChunk);
            } catch (IOException e) {
                this.generatingChunks.remove(globalPos);
                throw new RuntimeException(e);
            }

            // Mark the chunk as ready.
            builtChunk.ready = true;

            // Chunk isn't generating anymore.
            this.generatingChunks.remove(globalPos);

            return builtChunk;
        }

        /**
         * Opens a chunk at a specified position. If it isn't loaded yet, it will defer generating the chunk.
         *
         * @param pos       the region local position of the chunk to open.
         * @param globalPos the global position.
         * @return the loaded/generated chunk.
         */
        public @Nullable ServerChunk openChunk(ChunkPos pos, ChunkPos globalPos) {
            this.validateThread();

            @Nullable ServerChunk loadedChunk = this.getChunk(pos);
            if (loadedChunk == null) {
                this.generateChunk(pos, globalPos);
                return null;
            }

            var loadedAt = loadedChunk.getPos();
            if (!loadedAt.equals(pos)) {
                throw new IllegalStateError("Chunk requested to load at %s got loaded at %s instead".formatted(pos, loadedAt));
            }

            return loadedChunk;
        }

        /**
         * Opens a chunk at a specified position. If it isn't loaded yet, it will defer generating the chunk.
         *
         * @param pos       the region local position of the chunk to open.
         * @param globalPos the global position.
         * @return the loaded/generated chunk.
         */
        public @Nullable ServerChunk openChunkNow(ChunkPos pos, ChunkPos globalPos) {
            this.validateThread();

            @Nullable ServerChunk loadedChunk = this.getChunk(pos);
            if (loadedChunk == null) {
                return this.generateChunkNow(pos, globalPos);
            }

            var loadedAt = loadedChunk.getPos();
            if (!loadedAt.equals(pos)) {
                throw new IllegalStateError("Chunk requested to load at %s got loaded at %s instead".formatted(pos, loadedAt));
            }

            return loadedChunk;
        }

        public RegionPos getPos() {
            return this.pos;
        }
    }

    /**
     * Represents a collection of regions.
     *
     * @author XyperCode
     * @since 0.1.0
     */
    public static class RegionStorage {
        private final Map<RegionPos, Region> regions = new ConcurrentHashMap<>();

        /**
         * Saves a region to an output stream.
         *
         * @param region  the region to save.
         * @param stream  the output stream to save to.
         * @param dispose if true, the region will be disposed after saving.
         * @throws IOException if an I/O error occurs.
         */
        @ApiStatus.Internal
        public void save(Region region, DataOutputStream stream, boolean dispose) throws IOException {
            var pos = region.pos();

            // Write region metadata.
            stream.writeInt(region.dataVersion);
            stream.writeUTF(region.lastPlayedIn);
            stream.writeInt(pos.x());
            stream.writeInt(pos.z());
            stream.writeInt(World.REGION_SIZE);

            // Write chunks to the region file.
            var chunks = region.getChunks();
            stream.writeShort(chunks.size());
            var idx = 0;
            for (var chunk : chunks) {
                if (idx >= World.REGION_SIZE * World.REGION_SIZE)
                    throw new IllegalArgumentException("Too many chunks in region!");
                var localChunkPos = World.toLocalChunkPos(chunk.getPos());
                stream.writeByte(localChunkPos.x());
                stream.writeByte(localChunkPos.z());
                chunk.save().write(stream);
                idx++;
            }

            stream.flush();

            // Dispose the region if requested.
            if (dispose) {
                this.regions.remove(region.getPos());
                UltracraftServer.invokeAndWait(region::dispose);
            }
        }

        /**
         * Loads a region from an input stream.
         *
         * @param world  the world to load the region in.
         * @param stream the input stream to load from.
         * @return the loaded region.
         * @throws IOException if an I/O error occurs.
         */
        public Region load(ServerWorld world, DataInputStream stream) throws IOException {
            // Read region metadata.
            var dataVersion = stream.readInt();
            var lastPlayedIn = stream.readUTF();
            var x = stream.readInt();
            var z = stream.readInt();
            var regionSize = stream.readInt();
            var worldOffsetX = x * World.REGION_SIZE;
            var worldOffsetZ = z * World.REGION_SIZE;

            // Read chunks from region file.
            Map<ChunkPos, ServerChunk> chunkMap = new HashMap<>();
            var maxIdx = stream.readUnsignedShort();
            for (var idx = 0; idx <= maxIdx; idx++) {
                // Read local chunk pos.
                var chunkX = stream.readUnsignedByte();
                var chunkZ = stream.readUnsignedByte();

                // Validate chunk coordinates.
                Preconditions.checkElementIndex(chunkX, World.REGION_SIZE, "Invalid chunk X position");
                Preconditions.checkElementIndex(chunkZ, World.REGION_SIZE, "Invalid chunk Z position");

                // Create local and global chunk coordinates.
                var localChunkPos = new ChunkPos(chunkX, chunkZ);
                var globalChunkPos = new ChunkPos(chunkX + x * World.REGION_SIZE * World.REGION_SIZE, chunkZ + z * World.REGION_SIZE);

                // Load server chunk.
                var chunk = ServerChunk.load(world, globalChunkPos, MapType.read(stream));
                chunkMap.put(localChunkPos, chunk);
            }

            var regionPos = new RegionPos(x, z);

            // Chceck if region already exists, if so, then throw an error.
            var oldRegion = this.regions.get(regionPos);
            if (oldRegion != null) {
                throw new OverwriteError("Tried to overwrite region %s".formatted(regionPos));
            }

            // Create region instance.
            var region = new Region(world, regionPos, chunkMap);
            this.regions.put(regionPos, region);
            return region;
        }

        /**
         * Gets a loaded region from the storage map based on the given chunk position.
         *
         * @param chunkPos the chunk position.
         * @return the loaded region, or null if it isn't loaded.
         */
        @Nullable
        public Region getRegionAt(ChunkPos chunkPos) {
            var regionX = chunkPos.x() / 32;
            var regionZ = chunkPos.z() / 32;

            return this.getRegion(regionX, regionZ);
        }

        /**
         * Gets a loaded region from the storage map.
         *
         * @param regionX the X-coordinate of the region.
         * @param regionZ the Z-coordinate of the region.
         * @return the loaded region, or null if it isn't loaded.
         */
        @Nullable
        private Region getRegion(int regionX, int regionZ) {
            return this.getRegion(new RegionPos(regionX, regionZ));
        }

        /**
         * Gets a loaded region from the storage map.
         *
         * @param regionPos the region position.
         * @return the loaded region, or null if it isn't loaded.
         */
        @Nullable
        private Region getRegion(RegionPos regionPos) {
            return this.regions.get(regionPos);
        }
    }
}

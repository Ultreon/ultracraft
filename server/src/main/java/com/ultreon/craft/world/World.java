package com.ultreon.craft.world;

import com.badlogic.gdx.math.Vector3;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.block.entity.BlockEntity;
import com.ultreon.craft.crash.CrashCategory;
import com.ultreon.craft.crash.CrashLog;
import com.ultreon.craft.entity.DroppedItem;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.events.BlockEvents;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.menu.ContainerMenu;
import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.util.Utils;
import com.ultreon.craft.util.*;
import com.ultreon.craft.world.ServerWorld.Region;
import com.ultreon.craft.world.gen.WorldAccess;
import com.ultreon.craft.world.gen.biome.Biomes;
import com.ultreon.data.types.LongType;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.*;

/**
 * Base class for client/server worlds.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @see Chunk
 * @see Region
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
@ApiStatus.NonExtendable
@ParametersAreNonnullByDefault
public abstract class World implements ServerDisposable, WorldAccess {
    public static final int CHUNK_SIZE = 16;
    /**
     * @deprecated use {@link #CHUNK_SIZE} instead
     */
    @Deprecated
    public static final int CHUNK_HEIGHT = CHUNK_SIZE;
    public static final Marker MARKER = MarkerFactory.getMarker("World");
    public static final int REGION_SIZE = 32;
    public static final Identifier OVERWORLD = new Identifier("overworld");
    public static final float SEA_LEVEL = 64;

    protected static final Logger LOGGER = LoggerFactory.getLogger(World.class);

    protected final Vec3i spawnPoint = new Vec3i();
    protected final long seed;
    private int renderedChunks;

    protected final Int2ReferenceMap<Entity> entitiesById = new Int2ReferenceArrayMap<>();
    protected final List<Entity> entities = new ArrayList<>();
    private int curId;
    private int totalChunks;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<ChunkPos> alwaysLoaded = new ArrayList<>();
    final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() / 3, 1));
    private boolean disposed;
    private final Set<ChunkPos> invalidatedChunks = new LinkedHashSet<>();
    private final List<ContainerMenu> menus = new ArrayList<>();
    private final DimensionInfo info = DimensionInfo.OVERWORLD;
    protected UUID uid = Utils.ZEROED_UUID;
    private final Map<Vec2i, ChunkColumnInfo> chunkColumns = new ConcurrentHashMap<>();

    protected World() {
        // Shh, the original seed was 512.
        this(512/*new Random().nextLong()*/);
    }

    protected World(long seed) {
        this.seed = seed;
    }

    public World(@Nullable LongType seed) {
        this(seed == null ? new Random().nextLong() : seed.getValue());
    }

    static List<ChunkPos> getChunksAround(World world, Vec3d pos) {
        int startX = (int) (pos.x - world.getRenderDistance() * World.CHUNK_SIZE);
        int startY = (int) (pos.y - world.getRenderDistance() * World.CHUNK_SIZE);
        int startZ = (int) (pos.z - world.getRenderDistance() * World.CHUNK_SIZE);
        int endX = (int) (pos.x + world.getRenderDistance() * World.CHUNK_SIZE);
        int endY = (int) (pos.y + world.getRenderDistance() * World.CHUNK_SIZE);
        int endZ = (int) (pos.z + world.getRenderDistance() * World.CHUNK_SIZE);

        List<ChunkPos> toCreate = new ArrayList<>();
        for (int x = startX; x <= endX; x += World.CHUNK_SIZE) {
            for (int y = startY; y >= endY; y -= World.CHUNK_SIZE) {
                for (int z = startZ; z <= endZ; z += World.CHUNK_SIZE) {
                    ChunkPos chunkPos = Utils.chunkPosFromBlockCoords(new Vec3d(x, y, z));
                    toCreate.add(chunkPos);
                    if (x >= pos.x - World.CHUNK_SIZE
                        && x <= pos.x + World.CHUNK_SIZE
                        && y >= pos.y - World.CHUNK_SIZE
                        && y <= pos.y + World.CHUNK_SIZE
                        && z >= pos.z - World.CHUNK_SIZE
                        && z <= pos.z + World.CHUNK_SIZE) {
                        chunkPos = Utils.chunkPosFromBlockCoords(new Vec3d(x, y, z));
                        toCreate.add(chunkPos);
                    }
                }
            }
        }

        return toCreate;
    }

    public static ChunkPos blockToChunkPos(Vector3 pos) {
        return new ChunkPos(Math.floorDiv((int) pos.x, World.CHUNK_SIZE), Math.floorDiv((int) pos.y, World.CHUNK_SIZE), Math.floorDiv((int) pos.z, World.CHUNK_SIZE));
    }

    public static ChunkPos blockToChunkPos(Vec3d pos) {
        return new ChunkPos(Math.floorDiv((int) pos.x, World.CHUNK_SIZE), Math.floorDiv((int) pos.y, World.CHUNK_SIZE), Math.floorDiv((int) pos.z, World.CHUNK_SIZE));
    }

    public static ChunkPos blockToChunkPos(Vec3i pos) {
        return new ChunkPos(Math.floorDiv(pos.x, World.CHUNK_SIZE), Math.floorDiv(pos.y, World.CHUNK_SIZE), Math.floorDiv(pos.z, World.CHUNK_SIZE));
    }

    public static ChunkPos toChunkPos(BlockPos pos) {
        return new ChunkPos(Math.floorDiv(pos.x(), World.CHUNK_SIZE), Math.floorDiv(pos.y(), World.CHUNK_SIZE), Math.floorDiv(pos.z(), World.CHUNK_SIZE));
    }

    public static ChunkPos toChunkPos(int x, int y, int z) {
        return new ChunkPos(Math.floorDiv(x, World.CHUNK_SIZE), Math.floorDiv(y, World.CHUNK_SIZE), Math.floorDiv(z, World.CHUNK_SIZE));
    }

    public static Vec3i toChunkVec(BlockPos pos) {
        return new Vec3i(Math.floorDiv(pos.x(), World.CHUNK_SIZE), Math.floorDiv(pos.y(), World.CHUNK_SIZE), Math.floorDiv(pos.z(), World.CHUNK_SIZE));
    }

    public static Vec3i toChunkVec(int x, int y, int z) {
        return new Vec3i(Math.floorDiv(x, World.CHUNK_SIZE), Math.floorDiv(y, World.CHUNK_SIZE), Math.floorDiv(z, World.CHUNK_SIZE));
    }

    public List<ChunkPos> getChunksAround(Vec3d pos) {
        int renderDistance = this.getRenderDistance();

        ChunkPos center = World.toChunkPos((int) pos.x, (int) pos.y, (int) pos.z);

        UltracraftServer.LOGGER.debug(String.format("Center: (%s)", center));

        int startX = center.x() - renderDistance;
        int startY = center.y() - renderDistance;
        int startZ = center.z() - renderDistance;
        int endX = center.x() + renderDistance;
        int endY = center.y() + renderDistance;
        int endZ = center.z() + renderDistance;

        ChunkPos start = World.blockToChunkPos(new Vec3i(startX, startY, startZ));
        ChunkPos end = World.blockToChunkPos(new Vec3i(endX, endY, endZ));

//        UltracraftServer.LOGGER.debug(String.format("Start: (%s) - End: (%s)", start, end));

        List<ChunkPos> toCreate = new ArrayList<>();
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++){
                for (int z = startZ; z <= endZ; z++) {
                    ChunkPos chunkPos = new ChunkPos(x, y, z);
//                    UltracraftServer.LOGGER.debug(String.format("Adding: (%s)", chunkPos));
//                    Vec3d chunkVec = new Vec3d(x, y, z);
                    double abs = Math.abs(pos.d().div(World.CHUNK_SIZE).dst(x, y, z));
                    if (abs < renderDistance) {
//                        UltracraftServer.LOGGER.debug(String.format("Actually Adding: (%s)", chunkPos));
                        toCreate.add(chunkPos);
                    } else {
//                        UltracraftServer.LOGGER.debug(String.format("Not Adding: (%s) - Too Far: (%s) < (%s)", chunkPos, abs, renderDistance));
                    }
                }
            }
        }

        return toCreate;
    }

    protected abstract int getRenderDistance();

    private boolean shouldStayLoaded(ChunkPos pos) {
		return this.isSpawnChunk(pos) || this.isAlwaysLoaded(pos);
    }

    public boolean isAlwaysLoaded(ChunkPos pos) {
        return this.alwaysLoaded.contains(pos);
    }

    @Deprecated
    private CompletableFuture<Boolean> unloadChunkAsync(ChunkPos chunkPos) {
        return this.unloadChunkAsync(Objects.requireNonNull(this.getChunk(chunkPos), "Chunk not loaded: " + chunkPos));
    }

    @Deprecated
    @CanIgnoreReturnValue
    private CompletableFuture<Boolean> unloadChunkAsync(@NotNull Chunk chunk) {
        synchronized (chunk.lock) {
            return CompletableFuture.supplyAsync(() -> this.unloadChunk(chunk, chunk.getPos()), this.executor).exceptionally(throwable -> {
                World.fail(throwable, "Failed to unload chunk:");
                return false;
            });
        }
    }

    private static void fail(Throwable throwable, String msg) {
        if (throwable instanceof CompletionException e && e.getCause() instanceof Error error)
            UltracraftServer.get().crash(throwable);
        if (throwable instanceof Error error)
            UltracraftServer.get().crash(throwable);

        World.LOGGER.error(msg, throwable);
    }

    public boolean unloadChunk(@NotNull ChunkPos chunkPos) {
        this.checkThread();

        Chunk chunk = this.getChunk(chunkPos);
        if (chunk == null) return true;
        return this.unloadChunk(chunk, chunkPos);
    }

    @CanIgnoreReturnValue
    protected abstract boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkPos pos);

    /**
     * Casts a ray in the world and returns the result.
     * This uses the default of {@code 5} blocks.
     *
     * @param ray the ray to cast
     * @return the result
     */
    public HitResult rayCast(Ray ray) {
        return WorldRayCaster.rayCast(new HitResult(ray), this);
    }

    /**
     * Casts a ray in the world and returns the result.
     *
     * @param ray      the ray to cast
     * @param distance the maximum distance that the ray can travel
     * @return the result
     */
    public HitResult rayCast(Ray ray, float distance) {
        HitResult hitResult = new HitResult(ray, distance);
        return WorldRayCaster.rayCast(hitResult, this);
    }

    /**
     * Sets the block at the specified coordinates, with the given block type.
     *
     * @param x     the x-coordinate
     * @param y     the y-coordinate
     * @param z     the z-coordinate
     * @param block the block type to set
     * @return true if the block was successfully set, false otherwise
     */
    public boolean set(int x, int y, int z, Block block) {
        this.checkThread();

        BlockEvents.SET_BLOCK.factory().onSetBlock(this, new BlockPos(x, y, z), block);

        Chunk chunk = this.getChunkAt(x, y, z);
        if (chunk == null) return false;

        BlockPos cp = World.localizeBlock(x, y, z);
        return chunk.set(cp.x(), cp.y(), cp.z(), block);
    }

    /**
     * Gets a block at the specified coordinates.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param z the z-coordinate
     * @return the block at the specified coordinates
     */
    public Block get(int x, int y, int z) {
        this.checkThread();

        Chunk chunkAt = this.getChunkAt(x, y, z);
        if (chunkAt == null) return Blocks.AIR;
        if (!chunkAt.ready) return Blocks.AIR;

        BlockPos cp = World.localizeBlock(x, y, z);
        return chunkAt.getFast(cp.x(), cp.y(), cp.z());
    }

    protected abstract void checkThread();

    /**
     * Converts a block position to a chunk space block position.
     *
     * @param pos the world space block position
     * @return the block position in chunk space
     */
    public static BlockPos localize(BlockPos pos) {
        return World.localizeBlock(pos.x(), pos.y(), pos.z());
    }

    /**
     * Converts a block position to a chunk space block position.
     *
     * @param x the x coordinate in world space
     * @param y the y coordinate in world space
     * @param z the z coordinate in world space
     * @return the block position in chunk space
     */
    public static BlockPos localizeBlock(int x, int y, int z) {
        return new BlockPos(localize(x), localize(y), localize(z));
    }

    public static int localize(int coordinate) {
        int localized = coordinate % World.CHUNK_SIZE;
        return localized < 0 ? localized + World.CHUNK_SIZE : localized;
    }

    /**
     * Converts a block position to a chunk space block position.
     *
     * @param x   the x coordinate in world space
     * @param y   the y coordinate in world space
     * @param z   the z coordinate in world space
     * @param tmp a temporary vector to store the result
     * @return the block position in chunk space
     */
    public static Vec3i localizeBlock(int x, int y, int z, Vec3i tmp) {
        tmp.x = x % World.CHUNK_SIZE;
        tmp.y = y % World.CHUNK_SIZE;
        tmp.z = z % World.CHUNK_SIZE;

        if (tmp.x < 0) tmp.x += World.CHUNK_SIZE;
        if (tmp.y < 0) tmp.y += World.CHUNK_SIZE;
        if (tmp.z < 0) tmp.z += World.CHUNK_SIZE;

        return tmp;
    }

    /**
     * Converts a world space chunk position to a region space chunk
     *
     * @param x the x coordinate in world space
     * @param z the z coordinate in world space
     * @return the chunk position in region space
     * @deprecated use {@link #localizeChunk(int, int, int)} instead
     */
    @Deprecated
    public static ChunkPos localizeChunk(int x, int z) {
        return localizeChunk(x, 0, z);
    }

    /**
     * Converts a world space chunk position to a region space chunk
     *
     * @param x the x coordinate in world space
     * @param y the y coordinate in world space
     * @param z the z coordinate in world space
     * @return the chunk position in region space
     */
    public static ChunkPos localizeChunk(int x, int y, int z) {
        int cx = x % World.REGION_SIZE;
        int cy = y % World.REGION_SIZE;
        int cz = z % World.REGION_SIZE;

        if (cx < 0) cx += World.REGION_SIZE;
        if (cy < 0) cy += World.REGION_SIZE;
        if (cz < 0) cz += World.REGION_SIZE;

        return new ChunkPos(cx, cy, cz);
    }

    /**
     * Converts a world space chunk position to a region space chunk
     *
     * @param pos the chunk position in world space
     * @return the chunk position in region space
     */
    public static ChunkPos localize(ChunkPos pos) {
        return World.localizeChunk(pos.x(), pos.y(), pos.z());
    }

    @Nullable
    public abstract Chunk getChunk(ChunkPos pos);

    @Deprecated
    public Chunk getChunk(int x, int z) {
        return this.getChunk(new ChunkPos(x, 0, z));
    }

    public Chunk getChunk(int x, int y, int z) {
        return this.getChunk(new ChunkPos(x, y, z));
    }

    @Override
    public Chunk getChunk(Vec3i pos) {
        return (Chunk) WorldAccess.super.getChunk(pos);
    }

    @Override
    public @Nullable Chunk getChunkAt(Vec3i pos) {
        return (Chunk) WorldAccess.super.getChunkAt(pos);
    }

    @Nullable
    public Chunk getChunkAt(int x, int y, int z) {
        return (Chunk) WorldAccess.super.getChunkAt(x, y, z);
    }

    @Nullable
    public Chunk getChunkAt(BlockPos pos) {
        return this.getChunkAt(pos.x(), pos.y(), pos.z());
    }

    public boolean isOutOfWorldBounds(BlockPos pos) {
        return pos.y() < -30000000 || pos.y() >= 30000000
                || pos.x() < -30000000 || pos.x() > 30000000
                || pos.z() < -30000000 || pos.z() > 30000000;
    }

    public boolean isOutOfWorldBounds(int x, int y, int z) {
        return y < -30000000 || y > 30000000
                || x < -30000000 || x > 30000000
                || z < -30000000 || z > 30000000;
    }

    /**
     * Get the highest block in a column.
     *
     * @param x the x coordinate of the column
     * @param z the z coordinate of the column
     * @return The highest block in the column, or -1 if the chunk isn't loaded.
     */
    public int getHighest(int x, int z) {
        Chunk chunkAt = this.getChunkAt(x, getHighestChunkY(x, z), z);
        if (chunkAt == null) return Integer.MIN_VALUE;

        return chunkAt.getHighestBlock(x, z);
    }

    private int getHighestChunkY(int x, int z) {
        ChunkColumnInfo chunkColumnInfo = getChunkColumnInfo(x, z);
        if (chunkColumnInfo == null) return Integer.MIN_VALUE;
        return chunkColumnInfo.getHighestChunk() * World.CHUNK_SIZE;
    }

    private @Nullable ChunkColumnInfo getChunkColumnInfo(int x, int z) {
        return this.chunkColumns.get(new Vec2i(x, z));
    }

    /**
     * @deprecated Impossible to use due to infinite world height and depth.
     */
    @Deprecated
    public void setColumn(int x, int z, Block block) {
        this.setColumn(x, z, 256, block);
    }

    public void setColumn(int x, int z, int maxY, Block block) {
        if (this.getChunkAt(x, maxY, z) == null) return;

        // FIXME optimize
        for (; maxY > 0; maxY--) {
            this.set(x, maxY, z, block);
        }
    }

    /**
     * Sets the specified block in a 3D area defined by the given coordinates and dimensions.
     *
     * @param x      the x-coordinate of the starting point
     * @param y      the y-coordinate of the starting point
     * @param z      the z-coordinate of the starting point
     * @param width  the width of the 3D area
     * @param height the height of the 3D area
     * @param depth  the depth of the 3D area
     * @param block  the block to be set in the specified area
     * @return a {@link CompletableFuture} representing the asynchronous operation
     * @see #set(int, int, int, Block)
     */
    public CompletableFuture<Void> set(int x, int y, int z, int width, int height, int depth, Block block) {
        return CompletableFuture.runAsync(() -> {
            int curX = x, curY = y, curZ = z;
            int startX = Math.max(curX, 0);
            int startY = Math.max(curY, 0);
            int startZ = Math.max(curZ, 0);
            int endX = Math.min(width, curX + width);
            int endY = Math.min(height, curY + height);
            int endZ = Math.min(depth, curZ + depth);

            // FIXME optimize
            for (curY = startY; curY < endY; curY++) {
                for (curZ = startZ; curZ < endZ; curZ++) {
                    for (curX = startX; curX < endX; curX++) {
                        int blkX = curX;
                        int blkY = curY;
                        int blkZ = curZ;
                        UltracraftServer.invoke(() -> this.set(blkX, blkY, blkZ, block));
                    }
                }
            }
        });
    }

    public abstract Collection<? extends Chunk> getLoadedChunks();

    public boolean isChunkInvalidated(Chunk chunk) {
        return this.invalidatedChunks.contains(chunk.getPos());
    }

    @ApiStatus.Internal
    public void updateNeighbours(Chunk chunk) {
        ChunkPos pos = chunk.getPos();

        // X-Axis
        this.updateChunk(this.getChunk(new ChunkPos(pos.x() - 1, pos.y(), pos.z())));
        this.updateChunk(this.getChunk(new ChunkPos(pos.x() + 1, pos.y(), pos.z())));

        // Y-Axis
        this.updateChunk(this.getChunk(new ChunkPos(pos.x(), pos.y() - 1, pos.z())));
        this.updateChunk(this.getChunk(new ChunkPos(pos.x(), pos.y() + 1, pos.z())));

        // Z-Axis
        this.updateChunk(this.getChunk(new ChunkPos(pos.x(), pos.y(), pos.z() - 1)));
        this.updateChunk(this.getChunk(new ChunkPos(pos.x(), pos.y(), pos.z() + 1)));
    }

    @ApiStatus.Internal
    public void updateChunkAndNeighbours(Chunk chunk) {
        ChunkPos pos = chunk.getPos();
        this.updateChunk(chunk);
        this.updateNeighbours(chunk);
    }

    @ApiStatus.Internal
    public void updateChunk(@Nullable Chunk chunk) {
        if (chunk == null) return;
        this.invalidatedChunks.add(chunk.getPos());
    }

    /**
     * <b>NOTE:</b> This method is obsolete, {@link #spawn(Entity, MapType)} exists with more functionality.
     */
    @ApiStatus.Obsolete
    public <T extends Entity> T spawn(T entity) {
        Preconditions.checkNotNull(entity, "Cannot spawn null entity");
        this.setEntityId(entity);
        this.entitiesById.put(entity.getId(), entity);
        return entity;
    }

    public <T extends Entity> T spawn(T entity, MapType spawnData) {
        Preconditions.checkNotNull(entity, "Cannot spawn null entity");
        Preconditions.checkNotNull(entity, "Cannot entity with nul spawn data");
        this.setEntityId(entity);
        entity.onPrepareSpawn(spawnData);
        this.entitiesById.put(entity.getId(), entity);
        return entity;
    }

    private <T extends Entity> void setEntityId(T entity) {
        Preconditions.checkNotNull(entity, "Cannot set entity id for null entity");
        int oldId = entity.getId();
        if (oldId > 0 && this.entitiesById.containsKey(oldId)) {
            throw new IllegalStateException("Entity already spawned: " + entity);
        }
        int newId = oldId > 0 ? oldId : this.nextId();
        entity.setId(newId);
    }

    private int nextId() {
        return this.curId++;
    }

    public void despawn(Entity entity) {
        this.entitiesById.remove(entity.getId());
    }

    public void despawn(int id) {
        this.entitiesById.remove(id);
    }

    public Entity getEntity(int id) {
        return this.entitiesById.get(id);
    }

    /**
     * Collision detection against blocks.
     *
     * @param box          The bounding box of an entity.
     * @param collideFluid If true, will check for fluid collision.
     * @return A list of bounding boxes that got collided with.
     */
    public List<BoundingBox> collide(BoundingBox box, boolean collideFluid) {
        List<BoundingBox> boxes = new ArrayList<>();
        int xMin = (int) Math.floor(box.min.x);
        int xMax = (int) Math.floor(box.max.x);
        int yMin = (int) Math.floor(box.min.y);
        int yMax = (int) Math.floor(box.max.y);
        int zMin = (int) Math.floor(box.min.z);
        int zMax = (int) Math.floor(box.max.z);

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    Block block = this.get(x, y, z);
                    if (block.hasCollider() && (!collideFluid || block.isFluid())) {
                        BoundingBox blockBox = block.getBoundingBox(x, y, z);
                        if (blockBox.intersects(box)) {
                            boxes.add(blockBox);
                        }
                    }
                }
            }
        }

        return boxes;
    }

    /**
     * Cleans up any used variables, executors, etc.
     */
    @Override
    @ApiStatus.Internal
    public void dispose() {
        this.disposed = true;
        this.executor.shutdownNow().clear();
        try {
            if (!this.executor.awaitTermination(15, TimeUnit.SECONDS))
                throw new Error("World async executor failed to shutdown in time.");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the amount of loaded chunks in the world.
     */
    public int getTotalChunks() {
        return this.totalChunks;
    }

    /**
     * Fills the crash log with information about the world.
     * <p style="color: red;">NOTE: Internal API!</p>
     *
     * @param crashLog the crash log.
     */
    @ApiStatus.Internal
    public void fillCrashInfo(CrashLog crashLog) {
        CrashCategory cat = new CrashCategory("World Details");
        cat.add("Total chunks", this.totalChunks); // Too many chunks?
        cat.add("Rendered chunks", this.renderedChunks); // Chunk render overflow?
        cat.add("Seed", this.seed); // For weird world generation glitches

        crashLog.addCategory(cat);
    }

    /**
     * Checks if the given bounding box intersects any entities.
     *
     * @param boundingBox The bounding box to check with.
     * @return {@code true} if the bounding box intersects any entities, {@code false} otherwise.
     */
    public boolean intersectEntities(BoundingBox boundingBox) {
        for (Entity entity : this.entitiesById.values())
            if (entity.getBoundingBox().intersects(boundingBox)) return true;

        return false;
    }

    /**
     * Start breaking a block at the given position.
     *
     * @param breaking the position of the block.
     * @param breaker  the player breaking the block.
     */
    public void startBreaking(BlockPos breaking, Player breaker) {
        Chunk chunk = this.getChunkAt(breaking);
        if (chunk == null) return;
        BlockPos localBlockPos = World.localize(breaking);
        chunk.startBreaking(localBlockPos.x(), localBlockPos.y(), localBlockPos.z());
    }

    /**
     * Continue breaking a block at the given position.
     *
     * @param breaking the position of the block.
     * @param amount   the amount of breaking progress to make.
     * @param breaker  the player breaking the block.
     * @return A {@link BreakResult} which indicates the current status of the block breaking.
     */
    public BreakResult continueBreaking(BlockPos breaking, float amount, Player breaker) {
        Chunk chunk = this.getChunkAt(breaking);
        if (chunk == null) return BreakResult.FAILED;
        BlockPos localBlockPos = World.localize(breaking);
        Block block = this.get(breaking);

        if (block.isAir()) return BreakResult.FAILED;

        return chunk.continueBreaking(localBlockPos.x(), localBlockPos.y(), localBlockPos.z(), amount);
    }

    /**
     * Stop breaking a block at the given position.
     *
     * @param breaking the position of the block.
     * @param breaker  the player breaking the block.
     */
    public void stopBreaking(BlockPos breaking, Player breaker) {
        Chunk chunk = this.getChunkAt(breaking);
        if (chunk == null) return;
        BlockPos localBlockPos = World.localize(breaking);
        chunk.stopBreaking(localBlockPos.x(), localBlockPos.y(), localBlockPos.z());
    }

    /**
     * Get the break progress of a block at the given position.
     *
     * @param pos the position of the block.
     * @return The break progress of the block, or -1.0 if the block isn't being mined.
     */
    public float getBreakProgress(BlockPos pos) {
        Chunk chunk = this.getChunkAt(pos);
        if (chunk == null) return -1.0F;
        BlockPos localBlockPos = World.localize(pos);
        return chunk.getBreakProgress(localBlockPos.x(), localBlockPos.y(), localBlockPos.z());
    }

    /**
     * @return thr world seed, which is the base seed of the whole world.
     */
    public long getSeed() {
        return this.seed;
    }

    /**
     * Set the spawn point of the world.
     *
     * @param spawnX the x position of the spawn point
     * @param spawnZ the z position of the spawn point
     */
    public void setSpawnPoint(int spawnX, int spawnZ) {
        this.spawnPoint.set(spawnX, 256, spawnZ);
    }

    /**
     * Check if the given chunk is a spawn chunk.
     *
     * @param pos the chunk position
     * @return {@code true} if the chunk is a spawn chunk, {@code false} otherwise
     */
    public boolean isSpawnChunk(ChunkPos pos) {
        int x = pos.x();
        int y = pos.y();
        int z = pos.z();

        return this.spawnPoint.x - 1 <= x && this.spawnPoint.x + 1 >= x &&
               y >= 0 && y <= 16 &&
                this.spawnPoint.z - 1 <= z && this.spawnPoint.z + 1 >= z;
    }

    /**
     * Get the spawn point of the world.
     *
     * @return the spawn point
     */
    public BlockPos getSpawnPoint() {
        int highest = this.getHighest(this.spawnPoint.x, this.spawnPoint.z);
        if (highest != Integer.MIN_VALUE)
            this.spawnPoint.y = highest;

        return new BlockPos(this.spawnPoint);
    }

    public int getChunksLoaded() {
        return this.getLoadedChunks().size();
    }

    public boolean isDisposed() {
        return this.disposed;
    }

    public void onChunkUpdated(Chunk chunk) {
        this.invalidatedChunks.remove(chunk.getPos());
    }

    /**
     * Play a sound at the given location.
     *
     * @param sound the sound event to play.
     * @param x     the x position of the sound.
     * @param y     the y position of the sound.
     * @param z     the z position of the sound.
     */
    public void playSound(SoundEvent sound, double x, double y, double z) {

    }

    @ApiStatus.Internal
    public void closeMenu(ContainerMenu containerMenu) {
        if (!this.menus.contains(containerMenu)) return;
        this.menus.remove(containerMenu);
    }

    @ApiStatus.Internal
    public void openMenu(ContainerMenu containerMenu) {
        if (this.menus.contains(containerMenu)) return;
        this.menus.add(containerMenu);
    }

    /**
     * @return true if the world is running on the client, false otherwise.
     */
    public abstract boolean isClientSide();

    /**
     * @return true if the world is running on the server, false otherwise.
     */
    public boolean isServerSide() {
        return !this.isClientSide();
    }

    public Biome getBiome(BlockPos pos) {
        BlockPos localPos = World.localize(pos);
        Chunk chunk = this.getChunkAt(pos);
        if (chunk == null) return Biomes.PLAINS;
        return chunk.getBiome(localPos.x(), localPos.z());
    }

    public DimensionInfo getDimension() {
        return this.info;
    }

    public Collection<Entity> getEntities() {
        return this.entitiesById.values();
    }

    public <T extends Entity> Collection<Entity> getEntitiesByClass(Class<T> clazz) {
        return this.entitiesById.values().stream().filter(clazz::isInstance).toList();
    }

    public UUID getUID() {
        return this.uid;
    }

    public void setBlockEntity(BlockPos pos, BlockEntity blockEntity) {
        Chunk chunk = this.getChunkAt(pos);
        if (chunk == null) return;

        chunk.setBlockEntity(World.localize(pos), blockEntity);
    }

    public BlockEntity getBlockEntity(BlockPos pos) {
        Chunk chunk = this.getChunkAt(pos);
        if (chunk == null) return null;

        BlockPos localPos = World.localize(pos);
        return chunk.getBlockEntity(localPos.x(), localPos.y(), localPos.z());
    }

    public void drop(ItemStack itemStack, Vec3d position) {
        drop(itemStack, position, new Vec3d());
    }

    public void drop(ItemStack itemStack, Vec3d position, Vec3d velocity) {
        if (this.isClientSide()) return;

        this.spawn(new DroppedItem(this, itemStack, position, velocity));
    }

    public int ascend(int x, int y, int z) {
        while (true) {
            if (this.get(x, y, z).isAir())
                return y;
            y++;
        }
    }

    public int ascend(int x, int y, int z, int height) {
        while (true) {
            if (!this.get(x, y, z).isAir()) {
                y++;
                continue;
            }

            for (int i = 0; i < height; i++) {
                if (this.get(x, y + i, z).isAir()) {
                    return y;
                }
            }

            y++;
        }
    }
}
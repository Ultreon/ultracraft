package com.ultreon.craft.world;

import com.badlogic.gdx.math.Vector3;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.events.BlockEvents;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.tool.ToolItem;
import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.util.Utils;
import com.ultreon.craft.util.BoundingBox;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.util.Ray;
import com.ultreon.craft.util.WorldRayCaster;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.ultreon.libs.crash.v0.CrashCategory;
import com.ultreon.libs.crash.v0.CrashLog;
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

@SuppressWarnings({"UnusedReturnValue", "unused"})
@ParametersAreNonnullByDefault
public abstract class World implements ServerDisposable {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_HEIGHT = 256;
    public static final int WORLD_HEIGHT = 256;
    public static final int WORLD_DEPTH = 0;
    public static final Marker MARKER = MarkerFactory.getMarker("World");
    public static final int REGION_SIZE = 32;
    protected static final Logger LOGGER = LoggerFactory.getLogger(World.class);
    protected static int chunkLoads;

    private final Vec3i spawnPoint = new Vec3i();
    protected final long seed = 512;
    private int renderedChunks;

    protected final Int2ReferenceMap<Entity> entities = new Int2ReferenceArrayMap<>();
    private int curId;
    private int totalChunks;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<ChunkPos> alwaysLoaded = new ArrayList<>();
    final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);
    private boolean disposed;
    private final Set<ChunkPos> invalidatedChunks = new HashSet<>();

    public World() {
    }

    static List<ChunkPos> getChunksAround(World world, Vec3d pos) {
        int startX = (int) (pos.x - world.getRenderDistance() * World.CHUNK_SIZE);
        int startZ = (int) (pos.z - world.getRenderDistance() * World.CHUNK_SIZE);
        int endX = (int) (pos.x + world.getRenderDistance() * World.CHUNK_SIZE);
        int endZ = (int) (pos.z + world.getRenderDistance() * World.CHUNK_SIZE);

        List<ChunkPos> toCreate = new ArrayList<>();
        for (int x = startX; x <= endX; x += World.CHUNK_SIZE) {
            for (int z = startZ; z <= endZ; z += World.CHUNK_SIZE) {
                ChunkPos chunkPos = Utils.chunkPosFromBlockCoords(new Vec3d(x, 0, z));
                toCreate.add(chunkPos);
                if (x >= pos.x - World.CHUNK_SIZE
                        && x <= pos.x + World.CHUNK_SIZE
                        && z >= pos.z - World.CHUNK_SIZE
                        && z <= pos.z + World.CHUNK_SIZE) {
                    for (int y = -World.CHUNK_HEIGHT; y >= pos.y - World.CHUNK_HEIGHT * 2; y -= World.CHUNK_HEIGHT) {
                        chunkPos = Utils.chunkPosFromBlockCoords(new Vec3d(x, y, z));
                        toCreate.add(chunkPos);
                    }
                }
            }
        }

        return toCreate;
    }

    public static ChunkPos blockToChunkPos(Vector3 pos) {
        return new ChunkPos(Math.floorDiv((int) pos.x, CHUNK_SIZE), Math.floorDiv((int) pos.z, CHUNK_SIZE));
    }

    public static ChunkPos blockToChunkPos(Vec3d pos) {
        return new ChunkPos(Math.floorDiv((int) pos.x, CHUNK_SIZE), Math.floorDiv((int) pos.z, CHUNK_SIZE));
    }

    public static ChunkPos blockToChunkPos(Vec3i pos) {
        return new ChunkPos(Math.floorDiv(pos.x, CHUNK_SIZE), Math.floorDiv(pos.z, CHUNK_SIZE));
    }

    public static ChunkPos toChunkPos(BlockPos pos) {
        return new ChunkPos(Math.floorDiv(pos.x(), CHUNK_SIZE), Math.floorDiv(pos.z(), CHUNK_SIZE));
    }

    public List<ChunkPos> getChunksAround(Vec3d pos) {
        int startX = (int) (pos.x - this.getRenderDistance() * World.CHUNK_SIZE);
        int startZ = (int) (pos.z - this.getRenderDistance() * World.CHUNK_SIZE);
        int endX = (int) (pos.x + this.getRenderDistance() * World.CHUNK_SIZE);
        int endZ = (int) (pos.z + this.getRenderDistance() * World.CHUNK_SIZE);

        List<ChunkPos> toCreate = new ArrayList<>();
        for (int x = startX; x <= endX; x += World.CHUNK_SIZE) {
            for (int z = startZ; z <= endZ; z += World.CHUNK_SIZE) {
                ChunkPos chunkPos = Utils.chunkPosFromBlockCoords(new Vec3d(x, 0, z));
                toCreate.add(chunkPos);
                if (x >= pos.x - World.CHUNK_SIZE
                        && x <= pos.x + World.CHUNK_SIZE
                        && z >= pos.z - World.CHUNK_SIZE
                        && z <= pos.z + World.CHUNK_SIZE) {
                    for (int y = -World.CHUNK_HEIGHT; y >= pos.y - World.CHUNK_HEIGHT * 2; y -= World.CHUNK_HEIGHT) {
                        chunkPos = Utils.chunkPosFromBlockCoords(new Vec3d(x, y, z));
                        toCreate.add(chunkPos);
                    }
                }
            }
        }

        return toCreate;
    }

    protected abstract int getRenderDistance();

    private boolean shouldStayLoaded(ChunkPos pos) {
        return false;
//		return this.isSpawnChunk(pos) || this.isAlwaysLoaded(pos);
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

    public HitResult rayCast(Ray ray) {
        return WorldRayCaster.rayCast(new HitResult(ray), this);
    }

    public HitResult rayCast(Ray ray, float distance) {
        HitResult hitResult = new HitResult(ray, distance);
        return WorldRayCaster.rayCast(hitResult, this);
    }

    public void set(BlockPos blockPos, Block block) {
        this.checkThread();

        this.set(blockPos.x(), blockPos.y(), blockPos.z(), block);
    }

    public void set(int x, int y, int z, Block block) {
        this.checkThread();

        BlockEvents.SET_BLOCK.factory().onSetBlock(this, new BlockPos(x, y, z), block);

        Chunk chunk = this.getChunkAt(x, y, z);
        if (chunk == null) return;

        BlockPos cp = World.toLocalBlockPos(x, y, z);
        chunk.set(cp.x(), cp.y(), cp.z(), block);
    }

    public Block get(BlockPos pos) {
        this.checkThread();

        return this.get(pos.x(), pos.y(), pos.z());
    }

    public Block get(int x, int y, int z) {
        this.checkThread();

        Chunk chunkAt = this.getChunkAt(x, y, z);
        if (chunkAt == null) return Blocks.AIR;
        if (!chunkAt.ready) return Blocks.AIR;

        BlockPos cp = World.toLocalBlockPos(x, y, z);
        return chunkAt.getFast(cp.x(), cp.y(), cp.z());
    }

    protected abstract void checkThread();

    public static BlockPos toLocalBlockPos(BlockPos pos) {
        return World.toLocalBlockPos(pos.x(), pos.y(), pos.z());
    }

    public static BlockPos toLocalBlockPos(int x, int y, int z) {
        int cx = x % World.CHUNK_SIZE;
        int cy = y % World.CHUNK_HEIGHT;
        int cz = z % World.CHUNK_SIZE;

        if (cx < 0) cx += World.CHUNK_SIZE;
        if (cz < 0) cz += World.CHUNK_SIZE;

        return new BlockPos(cx, cy, cz);
    }

    public static ChunkPos toLocalChunkPos(int x, int z) {
        int cx = x % World.REGION_SIZE;
        int cz = z % World.REGION_SIZE;

        if (cx < 0) cx += World.REGION_SIZE;
        if (cz < 0) cz += World.REGION_SIZE;

        return new ChunkPos(cx, cz);
    }

    public static ChunkPos toLocalChunkPos(ChunkPos pos) {
        return World.toLocalChunkPos(pos.x(), pos.z());
    }

    @Nullable
    public abstract Chunk getChunk(ChunkPos pos);

    @Nullable
    public Chunk getChunkAt(int x, int y, int z) {
        return this.getChunkAt(new BlockPos(x, y, z));
    }

    @Nullable
    public Chunk getChunkAt(BlockPos pos) {
        int chunkX = Math.floorDiv(pos.x(), World.CHUNK_SIZE);
        int chunkZ = Math.floorDiv(pos.z(), World.CHUNK_SIZE);

        if (this.isOutOfWorldBounds(pos)) return null;

        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
        return this.getChunk(chunkPos);
    }

    private boolean isOutOfWorldBounds(BlockPos pos) {
        return pos.y() < World.WORLD_DEPTH || pos.y() > World.WORLD_HEIGHT;
    }

    public int getHighest(int x, int z) {
        Chunk chunkAt = this.getChunkAt(x, 0, z);
        if (chunkAt == null) return 0;

        // FIXME: Optimize by using a heightmap.
        for (int y = World.CHUNK_HEIGHT - 1; y > 0; y--) {
            if (this.get(x, y, z) != Blocks.AIR) return y + 1;
        }
        return 0;
    }

    public void setColumn(int x, int z, Block block) {
        this.setColumn(x, z, World.CHUNK_HEIGHT, block);
    }

    public void setColumn(int x, int z, int maxY, Block block) {
        if (this.getChunkAt(x, maxY, z) == null) return;

        // FIXME optimize
        for (; maxY > 0; maxY--) {
            this.set(x, maxY, z, block);
        }
    }

    @ApiStatus.Experimental
    public void set(int x, int y, int z, int width, int height, int depth, Block block) {
        int ix = x;
        int iy = y;
        int iz = z;
        int startX = Math.max(ix, 0);
        int endX = Math.min(width, ix + width);
        int startY = Math.max(iy, 0);
        int endY = Math.min(height, iy + height);
        int startZ = Math.max(iz, 0);
        int endZ = Math.min(depth, iz + depth);

        // FIXME optimize
        for (iy = startY; iy < endY; iy++) {
            for (iz = startZ; iz < endZ; iz++) {
                for (ix = startX; ix < endX; ix++) {
                    this.set(ix, iy, iz, block);
                }
            }
        }
    }

    public abstract Collection<? extends Chunk> getLoadedChunks();

    public boolean isChunkInvalidated(Chunk chunk) {
        return this.invalidatedChunks.contains(chunk.getPos());
    }

    @ApiStatus.Internal
    public void updateNeighbours(Chunk chunk) {
        ChunkPos pos = chunk.getPos();
        this.updateChunk(this.getChunk(new ChunkPos(pos.x() - 1, pos.z())));
        this.updateChunk(this.getChunk(new ChunkPos(pos.x() + 1, pos.z())));
        this.updateChunk(this.getChunk(new ChunkPos(pos.x(), pos.z() - 1)));
        this.updateChunk(this.getChunk(new ChunkPos(pos.x(), pos.z() + 1)));
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
        this.entities.put(entity.getId(), entity);
        return entity;
    }

    public <T extends Entity> T spawn(T entity, MapType spawnData) {
        Preconditions.checkNotNull(entity, "Cannot spawn null entity");
        Preconditions.checkNotNull(entity, "Cannot entity with nul spawn data");
        this.setEntityId(entity);
        entity.onPrepareSpawn(spawnData);
        this.entities.put(entity.getId(), entity);
        return entity;
    }

    private <T extends Entity> void setEntityId(T entity) {
        Preconditions.checkNotNull(entity, "Cannot set entity id for null entity");
        int oldId = entity.getId();
        if (oldId > 0 && this.entities.containsKey(oldId)) {
            throw new IllegalStateException("Entity already spawned: " + entity);
        }
        int newId = oldId > 0 ? oldId : this.nextId();
        entity.setId(newId);
    }

    private int nextId() {
        return this.curId++;
    }

    public void despawn(Entity entity) {
        this.entities.remove(entity.getId());
    }

    public void despawn(int id) {
        this.entities.remove(id);
    }

    public Entity getEntity(int id) {
        return this.entities.get(id);
    }

    public List<BoundingBox> collide(BoundingBox box, boolean collideFluid) {
        List<BoundingBox> boxes = new ArrayList<>();
        int xMin = (int) Math.floor(box.min.x);
        int xMax = (int) Math.floor(box.max.x);
        int yMin = (int) Math.floor(box.min.y);
        int yMax = (int) Math.floor(box.max.y);
        int zMin = (int) Math.floor(box.min.z);
        int zMax = (int) Math.floor(box.max.z);

        List<Block> blocks = new ArrayList<>();

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    Block block = this.get(x, y, z);
                    blocks.add(block);
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

    @Deprecated
    public int getRenderedChunks() {
        return this.renderedChunks;
    }

    public int getTotalChunks() {
        return this.totalChunks;
    }

    public void fillCrashInfo(CrashLog crashLog) {
        CrashCategory cat = new CrashCategory("World Details");
        cat.add("Total chunks", this.totalChunks); // Too many chunks?
        cat.add("Rendered chunks", this.renderedChunks); // Chunk render overflow?
        cat.add("Seed", this.seed); // For weird world generation glitches

        crashLog.addCategory(cat);
    }

    public boolean intersectEntities(BoundingBox boundingBox) {
        for (Entity entity : this.entities.values())
            if (entity.getBoundingBox().intersects(boundingBox)) return true;

        return false;
    }

    public void startBreaking(BlockPos breaking) {
        Chunk chunk = this.getChunkAt(breaking);
        if (chunk == null) return;
        BlockPos localBlockPos = this.toLocalBlockPos(breaking);
        chunk.startBreaking(localBlockPos.x(), localBlockPos.y(), localBlockPos.z());
    }

    public boolean continueBreaking(BlockPos breaking, float amount, Player player) {
        Chunk chunk = this.getChunkAt(breaking);
        if (chunk == null) return false;
        BlockPos localBlockPos = World.toLocalBlockPos(breaking);
        Block block = this.get(breaking);
        if (!chunk.continueBreaking(localBlockPos.x(), localBlockPos.y(), localBlockPos.z(), amount))
            return true;
        ItemStack stack = player.getSelectedItem();
        if (block.isToolRequired() && (!(stack.getItem() instanceof ToolItem toolItem) || toolItem.getToolType() != block.getEffectiveTool()))
            return true;
        if (player.inventory.addItems(block.getItemDrops()))
            return true;

        return true;
    }

    public void stopBreaking(BlockPos breaking) {
        Chunk chunk = this.getChunkAt(breaking);
        if (chunk == null) return;
        BlockPos localBlockPos = World.toLocalBlockPos(breaking);
        chunk.stopBreaking(localBlockPos.x(), localBlockPos.y(), localBlockPos.z());
    }

    public float getBreakProgress(BlockPos pos) {
        Chunk chunk = this.getChunkAt(pos);
        if (chunk == null) return -1.0F;
        BlockPos localBlockPos = World.toLocalBlockPos(pos);
        return chunk.getBreakProgress(localBlockPos.x(), localBlockPos.y(), localBlockPos.z());
    }

    public long getSeed() {
        return this.seed;
    }

    public void setSpawnPoint(int spawnX, int spawnZ) {
        this.spawnPoint.set(spawnX, this.getHighest(spawnX, spawnZ), spawnZ);
    }

    public boolean isSpawnChunk(ChunkPos pos) {
        int x = pos.x();
        int z = pos.z();

        return this.spawnPoint.x - 1 <= x && this.spawnPoint.x + 1 >= x &&
                this.spawnPoint.z - 1 <= z && this.spawnPoint.z + 1 >= z;
    }

    public BlockPos getSpawnPoint() {
        this.spawnPoint.y = this.getHighest(this.spawnPoint.x, this.spawnPoint.z);
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
        if (this.isChunkInvalidated(chunk)) {
            throw new ConcurrentModificationException("Chunk invalidated while updated.");
        }
    }

    public void playSound(SoundEvent hurtSound, double x, double y, double z) {

    }
}
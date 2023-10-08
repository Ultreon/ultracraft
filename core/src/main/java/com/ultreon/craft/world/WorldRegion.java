package com.ultreon.craft.world;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.data.types.MapType;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.ultreon.craft.UltreonCraft.LOGGER;

@Deprecated
public class WorldRegion {
    public static final int REGION_SIZE = 32;

    private final Map<ChunkPos, Chunk> chunks = new ConcurrentHashMap<>();
    private final World world;
    private final RegionPos pos;
    private MapType data = new MapType();
    private boolean ready = false;
    private boolean corrupt;
    protected final Object lock = new Object();
    private boolean disposed;
    private boolean loading = true;
    boolean initialized = false;

    public WorldRegion(World world, RegionPos pos, boolean loadAsync) {
        this(world, pos, loadAsync, region -> {});
    }

    public WorldRegion(World world, RegionPos pos, boolean loadAsync, Consumer<WorldRegion> onLoad) {
        this.world = world;
        this.pos = pos;

        if (world.getSavedWorld().regionExists(pos.x(), pos.z())) {
            if (loadAsync) {
                CompletableFuture.runAsync(() -> {
                    try {
                        this.load(onLoad);
                    } catch (IOException e) {
                        LOGGER.error(World.MARKER, "Failed to load region file: " +  pos, e);
                        this.corrupt = true;
                        this.loading = false;
                        return;
                    }
                    this.ready = true;
                    this.initialized = true;
                    this.loading = false;
                });
            } else load:{
                try {
                    this.load(onLoad);
                } catch (IOException e) {
                    LOGGER.error(World.MARKER, "Failed to load region file: " +  pos, e);
                    this.corrupt = true;
                    this.loading = false;
                    break load;
                }
                this.ready = true;
                this.initialized = true;
                this.loading = false;
            }
        }
    }

    public void save() throws IOException {
        if (!this.initialized) return;

        SavedWorld savedWorld = this.world.getSavedWorld();
        try {
            synchronized (this.lock) {
                for (Chunk chunk : this.chunks.values()) {
                    this.data.put(chunk.pos.toString(), chunk.save());
                }

                savedWorld.writeRegion(this.pos.x(), this.pos.z(), this.data);

                WorldEvents.SAVE_REGION.factory().onSaveRegion(this.world, this);
            }
        } catch (Exception e) {
            LOGGER.error(World.MARKER, String.format(Locale.ROOT, "Failed to save region file r%d.%d.ubo:", this.pos.x(), this.pos.z()), e);
        }
    }

    @CanIgnoreReturnValue
    public boolean saveChunk(ChunkPos chunkPos) {
        if (chunkPos.x() < 0 || chunkPos.x() > REGION_SIZE) return false;
        if (chunkPos.z() < 0 || chunkPos.z() > REGION_SIZE) return false;

        Chunk chunk;
        synchronized (this.lock) {
            chunk = this.chunks.get(chunkPos);
        }

        if (chunk == null) return false;
        MapType data = chunk.save();

        synchronized (this.lock) {
            this.data.put(chunkPos.toString(), data);
        }
        return true;
    }

    /**
     * Loads the chunk. Will return null if out of bounds or if the chunk isn't saved.
     * @param chunkPos this is the local chunk pos. Needs to be between 0,0 and 31,31.
     */
    @Nullable
    @CanIgnoreReturnValue
    public Chunk loadChunk(ChunkPos chunkPos) {
        if (chunkPos.x() < 0 || chunkPos.x() > REGION_SIZE) return null;
        if (chunkPos.z() < 0 || chunkPos.z() > REGION_SIZE) return null;

        ChunkPos worldChunkPos = new ChunkPos(chunkPos.x() + this.pos.x() * REGION_SIZE, chunkPos.z() + this.pos.z() * REGION_SIZE);
        MapType mapType = this.get(chunkPos);
        if (mapType == null) return null;

        Chunk chunk = Chunk.load(worldChunkPos, mapType);

        synchronized (this.lock) {
            this.chunks.put(chunkPos, chunk);
        }

        return chunk;
    }

    @Nullable
    @CheckReturnValue
    private MapType get(ChunkPos chunkPos) {
        return this.data.getMap(chunkPos.toString());
    }

    @Nullable
    @CanIgnoreReturnValue
    public Chunk getChunk(ChunkPos pos) {
        return this.chunks.get(pos);
    }

    public boolean isReady() {
        return this.ready;
    }

    public boolean isCorrupt() {
        return this.corrupt;
    }

    public boolean isDisposed() {
        return this.disposed;
    }

    public boolean isEmpty() {
        return this.chunks.isEmpty();
    }

    public boolean unload(boolean save, boolean force) {
        try {
            if (save) this.save();
        } catch (IOException e) {
            if (!force) {
                LOGGER.error(World.MARKER, "Failed to save when unloading region " + this.pos, e);
                return false;
            } else {
                LOGGER.error(World.MARKER, "Failed to save when unloading region " + this.pos + " (unloading anyways)", e);
            }
        }

        synchronized (this.lock) {
            this.chunks.forEach((pos1, chunk) -> chunk.dispose());
            this.chunks.clear();
        }
        return true;
    }

    /**
     * @return true if the world region has disposed successfully.
     */
    public boolean dispose() {
        synchronized (this.lock) {
            if (!this.unload(false, false)) return false;

            this.disposed = true;
            this.data = null;
        }
        return true;
    }

    @Deprecated
    public boolean dispose(boolean force) {
        if (force) this.disposeNow();
        else return this.dispose();
        return true;
    }

    public void disposeNow() {
        synchronized (this.lock) {
            this.disposed = true;

            this.unload(false, true);

            this.data = null;
        }
    }

    @CanIgnoreReturnValue
    public boolean unloadChunk(ChunkPos localChunkPos, boolean save) {
        return this.unloadChunk(localChunkPos, save, false);
    }

    @CanIgnoreReturnValue
    public boolean unloadChunk(ChunkPos localChunkPos, boolean save, boolean force) {
        if (localChunkPos.x() < 0 || localChunkPos.x() > REGION_SIZE) return false;
        if (localChunkPos.z() < 0 || localChunkPos.z() > REGION_SIZE) return false;

        boolean flag = true;
        synchronized (this.lock) {
            if (save && !(flag = this.saveChunk(localChunkPos)) && !force) return false;

            Chunk chunk = this.chunks.remove(localChunkPos);
            if (chunk == null) {
                LOGGER.warn("Tried to unload non-existing chunk: " + localChunkPos);
                return false;
            }

            chunk.dispose();
        }
        return flag;
    }

    private void load(Consumer<WorldRegion> onLoad) throws IOException {
        this.data = this.world.getSavedWorld().readRegion(this.pos.x(), this.pos.z());
        this.ready = true;
        onLoad.accept(this);

        WorldEvents.LOAD_REGION.factory().onLoadRegion(this.world, this);
    }

    public boolean putChunk(ChunkPos chunkPos, Chunk chunk, boolean overwrite) {
        synchronized (this.lock) {
            if (!overwrite)
                return this.chunks.putIfAbsent(chunkPos, chunk) == null;

            Chunk oldChunk = this.chunks.put(chunkPos, chunk);
            if (oldChunk != null)
                oldChunk.dispose();

            return true;
        }
    }

    @CheckReturnValue
    public Collection<Chunk> getChunks() {
        return this.chunks.values();
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public boolean isLoading() {
        return this.loading;
    }

    public RegionPos getPosition() {
        return this.pos;
    }
}

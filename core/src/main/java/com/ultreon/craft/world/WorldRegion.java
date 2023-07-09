package com.ultreon.craft.world;

import com.badlogic.gdx.utils.Disposable;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
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

public class WorldRegion implements Disposable {
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
                        LOGGER.error(World.MARKER, "Failed to load region file: {}", pos, e);
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
                    LOGGER.error(World.MARKER, "Failed to load region file: {}", pos, e);
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
                LOGGER.error(World.MARKER, "Failed to save when unloading region {}", this.pos, e);
                return false;
            } else {
                LOGGER.error(World.MARKER, "Failed to save when unloading region {} (unloading anyways)", this.pos, e);
            }
        }

        synchronized (this.lock) {
            this.chunks.forEach((pos1, chunk) -> chunk.dispose());
            this.chunks.clear();
        }
        return true;
    }

    public void dispose() {
        this.dispose(false);
    }

    public void dispose(boolean force) {
        synchronized (this.lock) {
            this.disposed = true;

            if (!this.unload(false, force) && !force) return;

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

            Chunk chunk = this.chunks.get(localChunkPos);
            if (chunk == null) return false;

            chunk.dispose();

            this.chunks.remove(localChunkPos);
        }
        return flag;
    }

    private void load(Consumer<WorldRegion> onLoad) throws IOException {
        this.data = this.world.getSavedWorld().readRegion(this.pos.x(), this.pos.z());
        this.ready = true;
        onLoad.accept(this);
    }

    public boolean putChunk(ChunkPos chunkPos, Chunk chunk, boolean overwrite) {
        synchronized (this.lock) {
            Chunk oldChunk = this.chunks.get(chunkPos);
            if (oldChunk != null) {
                if (overwrite) {
                    oldChunk.dispose();
                } else {
                    return false;
                }
            }
            this.chunks.put(chunkPos, chunk);
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

    public RegionPos getPosition() {
        return this.pos;
    }
}

package com.ultreon.craft.world;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.collection.PaletteStorage;
import com.ultreon.craft.debug.DebugFlags;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.craft.util.ValidationError;
import com.ultreon.craft.world.gen.TreeData;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static com.ultreon.craft.world.World.*;

@NotThreadSafe
@ApiStatus.NonExtendable
public abstract class Chunk implements ServerDisposable {
    public static final int VERTEX_SIZE = 6;
    private final ChunkPos pos;
    final Map<Vec3i, Float> breaking = new HashMap<>();
    protected final Object lock = new Object();
    protected boolean active;
    protected boolean modifiedByPlayer;
    protected boolean ready;
    //	protected Section[] sections;
    public final int size;
    public final int height;
    protected final Vec3i offset;
    @Nullable
    public TreeData treeData;
    private boolean disposed;
    @Deprecated
    protected boolean updateNeighbours;
    private final World world;
    protected List<Palette.@NotNull Index> blockData = new ArrayList<>();
    Block[] blocks = new Block[CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE];
    //	protected final Palette<Block> palette = new Palette<>(this::encodeBlock, this::decodeBlock);
    public final PaletteStorage<Block> storage;

    protected Chunk(World world, int size, int height, ChunkPos pos) {
        this(world, size, height, pos, new PaletteStorage<>(size * height * size));
    }

    protected Chunk(World world, int size, int height, ChunkPos pos, PaletteStorage<Block> storage) {
        this.world = world;

        this.offset = new Vec3i(pos.x() * CHUNK_SIZE, WORLD_DEPTH, pos.z() * CHUNK_SIZE);

        this.pos = pos;
        this.size = size;
        this.height = height;
        this.storage = storage;
    }

    public static Block decodeBlock(MapType inputData) {
        @Nullable String stringId = inputData.getString("id");
        if (stringId == null) {
            LOGGER.error("Unable to decode block, missing ID.");
            return Blocks.ERROR;
        }

        @Nullable Identifier id = Identifier.tryParse(stringId);

        if (id == null) {
            LOGGER.error("Unknown block: " + stringId);
            return Blocks.AIR;
        }
        return Registries.BLOCK.getValue(id);
    }

    // Serialize the chunk to a byte array
    public void serializeChunk(PacketBuffer buffer) {
        synchronized (this.lock) {
            this.storage.write(buffer);

            try {
                if (DebugFlags.CHUNK_BLOCK_DATA_DUMP) {
                    Path readDir = Path.of(".cache/ultracraft/_debug/chunk_block_data/before/");
                    if (Files.notExists(readDir)) Files.createDirectories(readDir);

                    Files.writeString(readDir.resolve("before_%s.%s.ucdebug".formatted(this.pos.x(), this.pos.z())), this.blockData.toString(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                }
            } catch (IOException ignored) {

            }

            // Debug network packet dump.
//            if (DebugFlags.CHUNK_PACKET_DUMP) {
//                Path readDir = Path.of(".cache/ultracraft/_debug/packet/written/");
//                if (Files.notExists(readDir)) Files.createDirectories(readDir);
//                Files.write(readDir.resolve("written_chunk_%s.%s.ucchunk".formatted(this.pos.x(), this.pos.z())), buffer, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
//            }
//            return byteArray;
        }
    }

    // Deserialize the chunk from a byte array
    public void deserializeChunk(PacketBuffer buffer) {
//        // Debug network packet dump.
//        if (DebugFlags.CHUNK_PACKET_DUMP) {
//            var readDir = Path.of(".cache/ultracraft/_debug/packet/read/");
//            if (Files.notExists(readDir)) Files.createDirectories(readDir);
//            Files.write(readDir.resolve("read_chunk_%s.%s.ucchunk".formatted(this.pos.x(), this.pos.z())), bytes, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
//        }

        this.storage.read(buffer, Chunk::decodeBlock);

        try {
            if (DebugFlags.CHUNK_BLOCK_DATA_DUMP) {
                Path readDir = Path.of(".cache/ultracraft/_debug/chunk_block_data/after/");
                if (Files.notExists(readDir)) Files.createDirectories(readDir);
                Files.writeString(readDir.resolve("after_%s.%s.ucdebug".formatted(this.pos.x(), this.pos.z())), this.blockData.toString(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException ignored) {

        }
    }

    public Block get(Vec3i pos) {
        if (this.disposed) return Blocks.BARRIER;
        return this.get(pos.x, pos.y, pos.z);
    }

    public Block get(int x, int y, int z) {
        if (this.disposed) return Blocks.BARRIER;
        if (this.isOutOfBounds(x, y, z)) return Blocks.AIR;
        return this.getFast(x, y, z);
    }

    public Block getFast(Vec3i pos) {
        return this.getFast(pos.x, pos.y, pos.z);
    }

    public Block getFast(int x, int y, int z) {
        if (this.disposed) return Blocks.BARRIER;
        int dataIdx = this.getIndex(x, y, z);

        Block block = this.storage.get(dataIdx);
        if (block == null) return Blocks.AIR;
        return block;
    }

    public void set(Vec3i pos, Block block) {
        this.set(pos.x, pos.y, pos.z, block);
    }

    public void set(int x, int y, int z, Block block) {
        if (this.isOutOfBounds(x, y, z)) return;
        this.setFast(x, y, z, block);
    }

    public void setFast(Vec3i pos, Block block) {
        this.setFast(pos.x, pos.y, pos.z, block);
    }

    public void setFast(int x, int y, int z, Block block) {
        if (this.disposed) return;
        int index = this.getIndex(x, y, z);

        this.storage.set(index, block);
    }

    private int getIndex(int x, int y, int z) {
        if (x >= 0 && x < CHUNK_SIZE && y >= 0 && y < CHUNK_HEIGHT && z >= 0 && z < CHUNK_SIZE) {
            return z * (this.size * this.height) + y * this.size + x;
        }
        return -1; // Out of bounds
    }

    private boolean isOutOfBounds(int x, int y, int z) {
        return x < 0 || x >= this.size || y < 0 || y >= this.height || z < 0 || z >= this.size;
    }

    @Override
    public void dispose() {
        synchronized (this.lock) {
            if (this.disposed) throw new ValidationError("Chunk is already disposed");
            this.disposed = true;
            this.ready = false;

            this.storage.dispose();
            this.blockData = null;
            this.blocks = null;
        }
    }

    @Override
    public String toString() {
        return "Chunk[x=" + this.getPos().x() + ", z=" + this.getPos().z() + "]";
    }

    public Vec3i getOffset() {
        return this.offset.cpy();
    }

    float getBreakProgress(float x, float y, float z) {
        Vec3i pos = new Vec3i((int) x, (int) y, (int) z);
        Float v = this.breaking.get(pos);
        if (v != null) {
            return v;
        }
        return -1.0F;
    }

    public void startBreaking(int x, int y, int z) {
        this.breaking.put(new Vec3i(x, y, z), 0.0F);
    }

    public void stopBreaking(int x, int y, int z) {
        this.breaking.remove(new Vec3i(x, y, z));
    }

    public boolean continueBreaking(int x, int y, int z, float amount) {
        Vec3i pos = new Vec3i(x, y, z);
        Float v = this.breaking.computeIfPresent(pos, (pos1, cur) -> Mth.clamp(cur + amount, 0, 1));
        if (v != null && v == 1.0F) {
            this.set(new Vec3i(x, y, z), Blocks.AIR);
            return true;
        }
        return false;
    }

    public Map<Vec3i, Float> getBreaking() {
        return Collections.unmodifiableMap(this.breaking);
    }

    public boolean isReady() {
        return this.ready;
    }

    public boolean isDisposed() {
        return this.disposed;
    }

    @Deprecated
    @ApiStatus.Internal
    public void onNeighboursUpdated() {

    }

    @ApiStatus.Internal
    public void onUpdated() {
        this.world.onChunkUpdated(this);
    }

    public World getWorld() {
        return this.world;
    }

    public boolean isActive() {
        return this.active;
    }

    public ChunkPos getPos() {
        return this.pos;
    }


    // TODO: Make faster with heightmaps.
    public int getHighest(int x, int z) {
        for (int y = CHUNK_HEIGHT; y > 0; y--) {
            if (!this.get(x, y, z).isAir()) {
                return y;
            }
        }
        return 0;
    }

    public int ascend(int x, int y, int z) {
        for (; y < CHUNK_HEIGHT; y++) {
            if (this.getFast(x, y, z).isAir()) {
                return y;
            }
        }
        return CHUNK_HEIGHT;
    }

    public enum Status {
        SUCCESS,
        SKIP,
        UNLOADED,
        FAILED
    }
}
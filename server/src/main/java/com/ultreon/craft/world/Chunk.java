package com.ultreon.craft.world;

import com.google.common.base.Preconditions;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.collection.PaletteStorage;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.craft.util.PosOutOfBoundsException;
import com.ultreon.craft.util.ValidationError;
import com.ultreon.craft.world.gen.TreeData;
import com.ultreon.craft.world.gen.biome.Biomes;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

import static com.ultreon.craft.world.World.*;
import static com.ultreon.libs.commons.v0.Mth.lerp;

/**
 * Represents a chunk in the world.
 * <p style="color: red;">NOTE: This class isn't meant to be extended</p>
 * <p style="color: red;">NOTE: This class isn't thread safe</p>
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
@NotThreadSafe
@ApiStatus.NonExtendable
public abstract class Chunk implements ServerDisposable, Iterable<ChunkSection> {
    public static final int VERTEX_SIZE = 6;
    private final ChunkPos pos;
    final Map<BlockPos, Float> breaking = new HashMap<>();
    protected final Object lock = new Object();
    protected boolean active;
    protected boolean ready;
    public final int size;
    public final int height;
    public final int depth;
    protected final Vec3i offset;
    @MonotonicNonNull
    @ApiStatus.Internal
    public TreeData treeData;
    private boolean disposed;
    private final World world;

    private final LightMap lightMap = new LightMap(CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE);

    private static final int MAX_LIGHT_LEVEL = 15;
    private static final float[] lightLevelMap = new float[Chunk.MAX_LIGHT_LEVEL + 1];

    static {
        for (int i = 0; i <= Chunk.MAX_LIGHT_LEVEL; i++) {
            double value = lerp(0.15f, 1.5f, (double) i / Chunk.MAX_LIGHT_LEVEL);
            Chunk.lightLevelMap[i] = (float) value;
        }
    }

    protected final ChunkSection[] sections;
    public final PaletteStorage<Biome> biomes;

    protected Chunk(World world, ChunkPos pos) {
        this(world, pos, new PaletteStorage<>(256, Biomes.PLAINS));
    }

    protected Chunk(World world, ChunkPos pos, PaletteStorage<Biome> biomes) {
        this(world, pos, Chunk.createSections(), biomes);
    }

    protected Chunk(World world, ChunkPos pos, ChunkSection[] sections, PaletteStorage<Biome> biomes) {
        Preconditions.checkNotNull(world, "World cannot be null");
        Preconditions.checkNotNull(pos, "Chunk position cannot be null");
        Preconditions.checkNotNull(sections, "Chunk sections cannot be null");
        Preconditions.checkNotNull(biomes, "Biome palette cannot be null");

        for (int i = 0, sectionsLength = sections.length; i < sectionsLength; i++) {
            ChunkSection section = sections[i];
            Preconditions.checkNotNull(section, "Chunk section cannot be null, found at index " + i);
        }

        this.world = world;

        this.offset = new Vec3i(pos.x() * CHUNK_SIZE, WORLD_DEPTH, pos.z() * CHUNK_SIZE);
        this.sections = sections;
        this.biomes = biomes;

        this.pos = pos;
        this.size = CHUNK_SIZE;
        this.height = CHUNK_HEIGHT;
        this.depth = WORLD_DEPTH;
    }

    private static ChunkSection[] createSections() {
        ChunkSection[] sections = new ChunkSection[CHUNK_HEIGHT / CHUNK_SIZE];
        for (int i = 0; i < sections.length; i++) {
            sections[i] = new ChunkSection();
        }

        return sections;
    }

    /**
     * Decodes a block from a UBO object.
     *
     * @param inputData The input data.
     * @return The decoded block data.
     */
    public static Block decodeBlock(MapType inputData) {
        @Nullable String stringId = inputData.getString("id");
        if (stringId == null) {
            LOGGER.error("Unable to decode block, missing ID.");
            return Blocks.ERROR;
        }

        @Nullable Identifier id = Identifier.tryParse(stringId);

        if (id == null) {
            LOGGER.error("Unknown block: {}", stringId);
            return Blocks.BARRIER;
        }
        return Registries.BLOCK.getValue(id);
    }

    public Block get(Vec3i pos) {
        if (this.disposed) return Blocks.BARRIER;
        return this.get(pos.x, pos.y, pos.z);
    }

    public Block get(BlockPos pos) {
        if (this.disposed) return Blocks.BARRIER;
        return this.get(pos.x(), pos.y(), pos.z());
    }

    public Block get(int x, int y, int z) {
        if (this.disposed) return Blocks.BARRIER;
        if (this.isOutOfBounds(x, y, z)) return Blocks.BARRIER;
        return this.getFast(x, y, z);
    }

    public Block getFast(Vec3i pos) {
        return this.getFast(pos.x, pos.y, pos.z);
    }

    public Block getFast(int x, int y, int z) {
        if (this.disposed) return Blocks.BARRIER;
        int dataIdx = this.getIndex(x, y, z);

        Block block = this.sections[y / CHUNK_SIZE].get(dataIdx);
        return block == null ? Blocks.AIR : block;
    }

    public void set(Vec3i pos, Block block) {
        this.set(pos.x, pos.y, pos.z, block);
    }

    public boolean set(int x, int y, int z, Block block) {
        if (this.isOutOfBounds(x, y, z)) return false;
        return this.setFast(x, y, z, block);
    }

    public void setFast(Vec3i pos, Block block) {
        this.setFast(pos.x, pos.y, pos.z, block);
    }

    public boolean setFast(int x, int y, int z, Block block) {
        if (this.disposed) return false;
        int index = this.getIndex(x, y, z);

        this.breaking.remove(new BlockPos(x, y, z));
        this.sections[y / CHUNK_SIZE].set(index, block);
        return true;
    }

    private int getIndex(int x, int y, int z) {
        x = x % CHUNK_SIZE;
        y = y % CHUNK_SIZE;
        z = z % CHUNK_SIZE;
        if (x >= 0 && y >= 0 && z >= 0) {
            return z * (this.size * this.size) + y * this.size + x;
        }
        return -1; // Out of bounds
    }

    public boolean isOutOfBounds(int x, int y, int z) {
        return x < 0 || x >= this.size || y < 0 || y >= this.height || z < 0 || z >= this.size;
    }

    @Override
    public void dispose() {
        synchronized (this.lock) {
            if (this.disposed) throw new ValidationError("Chunk is already disposed");
            this.disposed = true;
            this.ready = false;
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
        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
        Float v = this.breaking.get(pos);
        if (v != null) {
            return v;
        }
        return -1.0F;
    }

    public void startBreaking(int x, int y, int z) {
        this.breaking.put(new BlockPos(x, y, z), 0.0F);
    }

    public void stopBreaking(int x, int y, int z) {
        this.breaking.remove(new BlockPos(x, y, z));
    }

    public BreakResult continueBreaking(int x, int y, int z, float amount) {
        BlockPos pos = new BlockPos(x, y, z);
        Float v = this.breaking.computeIfPresent(pos, (pos1, cur) -> Mth.clamp(cur + amount, 0, 1));
        if (v != null && v == 1.0F) {
            this.breaking.remove(pos);
            return BreakResult.BROKEN;
        }
        return BreakResult.CONTINUE;
    }

    public Map<BlockPos, Float> getBreaking() {
        return Collections.unmodifiableMap(this.breaking);
    }

    public boolean isReady() {
        return this.ready;
    }

    public boolean isDisposed() {
        return this.disposed;
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

    /**
     * Find the highest block at the given position.
     *
     * @param x the x coordinate
     * @param z the z coordinate
     * @return The highest block Y coordinate.
     */
    // TODO: Make faster with heightmaps.
    public int getHighest(int x, int z) {
        for (int y = CHUNK_HEIGHT; y > 0; y--) {
            if (!this.get(x, y, z).isAir()) {
                return y;
            }
        }
        return 0;
    }

    /**
     * Find an empty space above the given position.
     *
     * @param x The x position.
     * @param y The y position to use as base.
     * @param z The z position.
     * @return The found position.
     */
    public int ascend(int x, int y, int z) {
        for (; y < CHUNK_HEIGHT; y++) {
            if (this.getFast(x, y, z).isAir()) {
                return y;
            }
        }
        return CHUNK_HEIGHT;
    }

    /**
     * Find an empty space with a given {@code height} above the given position.
     *
     * @param x      The x position.
     * @param y      The y position to use as base.
     * @param z      The z position.
     * @param height The height of the space.
     * @return The found position.
     */
    public int ascend(int x, int y, int z, int height) {
        for (; y < CHUNK_HEIGHT; y++) {
            if (!this.getFast(x, y, z).isAir()) continue;

            for (int i = 0; i < height; i++) {
                if (this.getFast(x, y + i, z).isAir()) return y;
            }
        }
        return CHUNK_HEIGHT;
    }

    public void setTreeData(TreeData treeData) {
        if (this.treeData != null) return;

        this.treeData = treeData;
    }

    protected int toFlatIndex(int x, int z) {
        return x + z * CHUNK_SIZE;
    }

    public Biome getBiome(int x, int y, int z) {
        int index = this.toFlatIndex(x, z);
        Biome biome = this.biomes.get(index);
        if (biome == null) throw new IllegalStateException("No biome at " + (this.offset.x + x) + ", " + (this.offset.y + y) + ", " + (this.offset.z + z));
        return biome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Chunk chunk = (Chunk) o;
        return Objects.equals(this.pos, chunk.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pos);
    }

    public float getLightLevel(int x, int y, int z) throws PosOutOfBoundsException {
        if(this.isOutOfBounds(x, y, z))
            throw new PosOutOfBoundsException();

        int sunlight = 15;
        int blockLight = this.lightMap.getBlockLight(x, y, z);
        return Chunk.lightLevelMap[Mth.clamp(sunlight + blockLight, 0, Chunk.MAX_LIGHT_LEVEL)];
    }

    public int getSunlight(int x, int y, int z) throws PosOutOfBoundsException {
        if(this.isOutOfBounds(x, y, z))
            throw new PosOutOfBoundsException(x + ", " + y + ", " + z);

        return 15;
    }

    public int getSunlight(Vec3i localBlockPos) {
        return this.getSunlight(localBlockPos.x, localBlockPos.y, localBlockPos.z);
    }

    public int getBlockLight(int x, int y, int z) throws PosOutOfBoundsException {
        if(this.isOutOfBounds(x, y, z))
            throw new PosOutOfBoundsException();

        return this.lightMap.getBlockLight(x, y, z);
    }

    public int getBlockLight(Vec3i localBlockPos) {
        return this.getBlockLight(localBlockPos.x, localBlockPos.y, localBlockPos.z);
    }

    public float getBrightness(int lightLevel) {
        if(lightLevel > Chunk.MAX_LIGHT_LEVEL)
            return 1;
        if(lightLevel < 0)
            return 0;
        return Chunk.lightLevelMap[lightLevel];
    }

    @NotNull
    @Override
    public Iterator<ChunkSection> iterator() {
        return Arrays.stream(this.sections).iterator();
    }

    /**
     * Chunk status for client chunk load response.
     *
     * @author <a href="https://github.com/XyperCode">XyperCode</a>
     */
    public enum Status {
        SUCCESS,
        SKIP,
        UNLOADED,
        FAILED
    }
}
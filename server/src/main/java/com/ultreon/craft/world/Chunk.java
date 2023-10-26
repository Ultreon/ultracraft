package com.ultreon.craft.world;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.ServerDisposable;
import com.ultreon.craft.util.HexTable;
import com.ultreon.craft.util.ValidationError;
import com.ultreon.craft.world.gen.TreeData;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.ultreon.craft.world.World.*;

@ApiStatus.NonExtendable
public class Chunk implements ServerDisposable {
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
	protected List<Palette.Index> blockData = new ArrayList<>();
	private Block[] blocks = new Block[CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE];
	protected final Palette<Block> palette = new Palette<>(this::encodeId, this::decodeId);

	protected Chunk(World world, int size, int height, ChunkPos pos) {
		this.world = world;

		this.offset = new Vec3i(pos.x() * CHUNK_SIZE, WORLD_DEPTH, pos.z() * CHUNK_SIZE);

		this.pos = pos;
		this.size = size;
		this.height = height;
    }

	private void encodeId(DataOutputStream dos, Block id) throws IOException {
		dos.writeUTF(id.getId().toString());
	}

	private Block decodeId(DataInputStream dataInputStream) throws IOException {
		Identifier id = new Identifier(dataInputStream.readUTF());
        return Registries.BLOCK.getValue(id);
	}

	// Serialize the chunk to a byte array
	public byte[] serializeChunk() throws IOException {
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		GZIPOutputStream gos = new GZIPOutputStream(fos);
		DataOutputStream dos = new DataOutputStream(gos);

		// Serialize the palette
		byte[] serializedPalette = this.palette.serializePalette();
		dos.writeShort(serializedPalette.length);
		dos.write(serializedPalette);

		// Serialize the block data
		dos.writeInt(this.blockData.size());
		for (Palette.Index paletteIndex : this.blockData) {
			dos.writeShort(paletteIndex != null ? paletteIndex.getValue() : -1);
		}

		dos.flush();
		gos.finish();
		dos.close();

		return fos.toByteArray();
	}

	// Deserialize the chunk from a byte array
	public void deserializeChunk(byte[] data) throws IOException {
		ByteArrayInputStream fis = new ByteArrayInputStream(data);
		GZIPInputStream gis = new GZIPInputStream(fis);
		DataInputStream dis = new DataInputStream(gis);

		// Deserialize the palette
		int paletteSize = dis.readUnsignedShort();
		byte[] paletteData = new byte[paletteSize];
		dis.readFully(paletteData);
		this.palette.deserializePalette(paletteData);

		// Deserialize the block data
		int dataSize = dis.readInt();
		while (this.blockData.size() <= dataSize) {
			this.blockData.add(Palette.Index.INVALID);
		}
		for (int i = 0; i < dataSize; i++) {
			int paletteIndex = dis.readShort();
			if (paletteIndex == -1) {
				this.blockData.set(i, Palette.Index.INVALID);
				continue;
			}
			var stub = new Palette.Index(paletteIndex);
			Palette.Index index = null;
			if (this.blockData.contains(stub)) index = this.blockData.get(this.blockData.indexOf(stub));
			if (index == null) index = new Palette.Index(paletteIndex);
			this.blockData.set(i, index);
		}

		dis.close();
	}

	public Block get(Vec3i pos) {
		if (this.disposed) return Blocks.AIR;
		return this.get(pos.x, pos.y, pos.z);
	}

	public Block get(int x, int y, int z) {
		if (this.disposed) return Blocks.AIR;
		if (this.isOutOfBounds(x, y, z)) return Blocks.AIR;
		return this.getFast(x, y, z);
	}

	public Block getFast(Vec3i pos) {
		return this.getFast(pos.x, pos.y, pos.z);
	}

	public Block getFast(int x, int y, int z) {
		synchronized (this.lock) {
			if (this.disposed) return Blocks.AIR;
			int dataIdx = this.getIndex(x, y, z);
			if (dataIdx >= 0 && dataIdx < this.blockData.size()) {
				Palette.Index paletteIdx = this.blockData.get(dataIdx);
				Block block = this.palette.get(paletteIdx);
				if (block == null) return Blocks.AIR;
				return block;
			}
			return Blocks.AIR; // Or throw an exception
		}
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
		synchronized (this.lock) {
			if (this.disposed) return;
			int index = this.getIndex(x, y, z);

			Block oldId = this.blocks[index];
			this.blocks[index] = block;
			boolean contains = ArrayUtils.contains(this.blocks, oldId);
			if (!contains && oldId != null) {
				Palette.Index index2 = this.palette.get(oldId);
				short removed = index2.getValue();
				this.blockData.removeIf(index1 -> index1.getValue() == removed);
				this.blockData.remove(index2);
				this.palette.remove(oldId);
			}
			Palette.Index paletteIndex = this.palette.add(block);
			while (this.blockData.size() <= index) {
                this.blockData.add(Palette.Index.INVALID); // Initialize with zeros
			}
            this.blockData.set(index, paletteIndex);
		}
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

            this.palette.clear();
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

}
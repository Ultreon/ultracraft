package com.ultreon.craft.world;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.render.world.ChunkMesh;
import com.ultreon.craft.world.gen.TreeData;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import static com.ultreon.craft.world.World.CHUNK_SIZE;
import static com.ultreon.craft.world.World.WORLD_DEPTH;

public class Chunk implements Disposable {
	public static final int VERTEX_SIZE = 6;
	public final ChunkPos pos;
	protected final Lock lock = new ReentrantLock();
	public Vector3 renderOffset = new Vector3();
	public ChunkMesh mesh;
	public ChunkMesh trasparentMesh;
    protected boolean modifiedByPlayer;
	protected boolean ready;
	private Section[] sections;
	public final int size;
	public final int height;
	private final Vec3i offset;
	private final int sizeTimesHeight;
	@Nullable
	public TreeData treeData;
	public boolean dirty;
	private boolean disposed;
	protected boolean updateNeighbours;

	public Chunk(int size, int height, ChunkPos pos) {
		int sectionCount = height / size;

		this.offset = new Vec3i(pos.x() * CHUNK_SIZE, WORLD_DEPTH, pos.z() * CHUNK_SIZE);

		this.pos = pos;
		this.sections = new Section[sectionCount];
		this.size = size;
		this.height = height;
		this.sizeTimesHeight = size * size;

		for (int i = 0; i < this.sections.length; i++) {
			this.sections[i] = new Section(new Vec3i(this.offset.x, this.offset.y + i * size, this.offset.z));
		}
	}

	public static Chunk load(ChunkPos pos, MapType mapType) {
		Chunk chunk = new Chunk(CHUNK_SIZE, World.CHUNK_HEIGHT, pos);
		chunk.load(mapType);
		return chunk;
	}

	void load(MapType chunkData) {
		ListType<MapType> sectionsData = chunkData.getList("Sections", new ListType<>());
		int y = 0;
		this.lock.lock();
		for (MapType sectionData : sectionsData) {
			this.sections[y].dispose();
			this.sections[y] = new Section(new Vec3i(this.offset.x, this.offset.y + y * this.size, this.offset.z), sectionData);
			y++;
		}
		this.lock.unlock();

		MapType extra = chunkData.getMap("Extra");
		if (extra != null) {
			WorldEvents.LOAD_CHUNK.factory().onLoadChunk(this, extra);
		}
	}

	public MapType save() {
		MapType chunkData = new MapType();
		ListType<MapType> sectionsData = new ListType<>();
		this.lock.lock();
		for (Section section : this.sections) {
			sectionsData.add(section.save());
		}
		this.lock.unlock();
		chunkData.put("Sections", sectionsData);

		MapType extra = new MapType();
		WorldEvents.SAVE_CHUNK.factory().onSaveChunk(this, extra);
		if (!extra.getValue().isEmpty()) {
			chunkData.put("Extra", extra);
		}
		return chunkData;
	}

	public Block get(Vec3i pos) {
		return this.get(pos.x, pos.y, pos.z);
	}

	public Block get(int x, int y, int z) {
		if (this.isOutOfBounds(x, y, z)) return Blocks.AIR;
		return this.getFast(x, y, z);
	}

	public Block getFast(Vec3i pos) {
		return this.getFast(pos.x, pos.y, pos.z);
	}

	public Block getFast(int x, int y, int z) {
		this.lock.lock();
		Block fast = this.sections[y / this.size].getFast(x, y % this.size, z);
		this.lock.unlock();
		return fast;
	}

	public void set(Vec3i pos, Block block) {
		this.set(pos.x, pos.y, pos.z, block);
	}

	public void set(int x, int y, int z, Block block) {
		if (this.isOutOfBounds(x, y, z)) return;
		this.setFast(x, y, z, block);
	}

	public void setFast(Vec3i pos, Block block) {
		this.set(pos.x, pos.y, pos.z, block);
	}

	public void setFast(int x, int y, int z, Block block) {
		this.lock.lock();
		this.sections[y / this.size].setFast(x, y % this.size, z, block);
		this.dirty = true;
		this.lock.unlock();
        this.updateNeighbours = true;
	}

	private boolean isOutOfBounds(int x, int y, int z) {
		return x < 0 || x >= this.size || y < 0 || y >= this.height || z < 0 || z >= this.size;
	}

	@Nullable
	public Section getSection(int sectionY) {
		this.lock.lock();
		if (sectionY < 0 || sectionY > this.sections.length){
			this.lock.unlock();
			return null;
		}
		Section section = this.sections[sectionY];
		this.lock.unlock();
		return section;
	}

	@Nullable
	public Section getSectionAt(int chunkY) {
		return this.getSection(chunkY / this.size);
	}

	private Vec3i reverse(int index) {
		int y = index / this.sizeTimesHeight;
		int z = (index - y * this.sizeTimesHeight) / this.size;
		int x = index - y * this.sizeTimesHeight - z * this.size;
		return new Vec3i(x, y, z);
	}

	@Override
	public void dispose() {
		this.lock.lock();
		this.disposed = true;
		this.ready = false;

		for (Section section : this.sections) {
			section.dispose();
		}
		this.sections = null;

		ChunkMesh chunkMesh = this.mesh;
		if (chunkMesh != null) {
			UltreonCraft.get().worldRenderer.free(this);
		}
		this.lock.unlock();
	}

	@Override
	public String toString() {
		return "Chunk[x=" + this.pos.x() + ", z=" + this.pos.z() + "]";
	}

	public Iterable<Section> getSections() {
		return Arrays.asList(this.sections);
	}

	public Vec3i getOffset() {
		return this.offset.cpy();
	}

	public void startBreaking(int x, int y, int z) {
		Section sectionAt = this.getSectionAt(y);
		if (sectionAt == null) return;
		sectionAt.startBreaking(x, y % CHUNK_SIZE, z);
	}

	public void continueBreaking(int x, int y, int z, float amount) {
		Section sectionAt = this.getSectionAt(y);
		if (sectionAt == null) return;
		sectionAt.continueBreaking(x, y % CHUNK_SIZE, z, amount);
	}

	public void stopBreaking(int x, int y, int z) {
		Section sectionAt = this.getSectionAt(y);
		if (sectionAt == null) return;
		sectionAt.stopBreaking(x, y % CHUNK_SIZE, z);
	}

	public float getBreakProgress(int x, int y, int z) {
		Section sectionAt = this.getSectionAt(y);
		if (sectionAt == null) return -1.0F;
		return sectionAt.getBreakProgress(x, y % CHUNK_SIZE, z);
	}

	public boolean isReady() {
		return this.ready;
	}

	public boolean isDisposed() {
		return this.disposed;
	}

	public void setDirty(boolean b) {
		this.dirty = false;
	}

	public void onNeighboursUpdated() {
		this.updateNeighbours = false;
	}
}
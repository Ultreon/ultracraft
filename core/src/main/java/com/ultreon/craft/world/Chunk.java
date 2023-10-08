package com.ultreon.craft.world;

import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.world.gen.TreeData;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static com.ultreon.craft.world.World.CHUNK_SIZE;
import static com.ultreon.craft.world.World.WORLD_DEPTH;

public class Chunk implements Disposable {
	public static final int VERTEX_SIZE = 6;
	public final ChunkPos pos;
	protected final Object lock = new Object();
	protected boolean modifiedByPlayer;
	protected boolean ready;
	private Section[] sections;
	public final int size;
	public final int height;
	private final Vec3i offset;
	private final int sizeTimesHeight;
	@Nullable
	public TreeData treeData;
	protected boolean dirty;

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
		for (MapType sectionData : sectionsData) {
			this.sections[y].dispose();
			this.sections[y] = new Section(new Vec3i(this.offset.x, this.offset.y + y * this.size, this.offset.z), sectionData);
			y++;
		}

		MapType extra = chunkData.getMap("Extra");
		if (extra != null) {
			WorldEvents.LOAD_CHUNK.factory().onLoadChunk(this, extra);
		}
	}

	public MapType save() {
		MapType chunkData = new MapType();
		ListType<MapType> sectionsData = new ListType<>();
		for (Section section : this.sections) {
			sectionsData.add(section.save());
		}
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
		synchronized (this.lock) {
			return this.sections[y / this.size].getFast(x, y % this.size, z);
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
		this.set(pos.x, pos.y, pos.z, block);
	}

	public void setFast(int x, int y, int z, Block block) {
		synchronized (this.lock) {
			this.sections[y / this.size].setFast(x, y % this.size, z, block);
			this.dirty = true;
		}
	}

	private boolean isOutOfBounds(int x, int y, int z) {
		return x < 0 || x >= this.size || y < 0 || y >= this.height || z < 0 || z >= this.size;
	}

	@Nullable
	public Section getSection(int sectionY) {
		synchronized (this.lock) {
			if (sectionY < 0 || sectionY > this.sections.length) return null;
			return this.sections[sectionY];
		}
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
	@SuppressWarnings("DataFlowIssue")
	public void dispose() {
		synchronized (this.lock) {
			this.ready = false;

			for (Section section : this.sections) {
				section.dispose();
			}
			this.sections = null;
		}
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

	public boolean isReady() {
		return this.ready;
	}
}
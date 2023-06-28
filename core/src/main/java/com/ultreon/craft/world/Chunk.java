package com.ultreon.craft.world;

import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.TextureManager;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.world.gen.TreeData;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec3i;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ultreon.craft.world.World.CHUNK_SIZE;

import static com.ultreon.craft.world.World.CHUNK_SIZE;
import static com.ultreon.craft.world.World.WORLD_DEPTH;

public class Chunk implements Disposable {
	public static final int VERTEX_SIZE = 6;
	public static final List<TextureRegion> BREAK_TEX = new ArrayList<>();
	final Map<Vec3i, Float> breaking = new HashMap<>();
	public final ChunkPos pos;
	protected final Object lock = new Object();
	protected boolean modifiedByPlayer;
	protected boolean ready;
	private Section[] sections;
	public final int size;
	public final int height;
	private final GridPoint3 offset;
	private final int sizeTimesHeight;
	public TreeData treeData;
	protected boolean dirty;
	protected boolean updateNeighbours;

	private final World world;

	public Chunk(World world, int size, int height, ChunkPos pos) {
		int sectionCount = height / size;

		this.offset = new GridPoint3(pos.x() * CHUNK_SIZE, WORLD_DEPTH, pos.z() * CHUNK_SIZE);

		this.world = world;
		this.pos = pos;
		this.sections = new Section[sectionCount];
		this.size = size;
		this.height = height;
		this.sizeTimesHeight = size * size;

		for (int i = 0; i < this.sections.length; i++) {
			this.sections[i] = new Section(new GridPoint3(this.offset.x, this.offset.y + i * size, this.offset.z));
		}
	}

	public static Chunk load(World world, ChunkPos pos, MapType mapType) {
		Chunk chunk = new Chunk(world, CHUNK_SIZE, World.CHUNK_HEIGHT, pos);
		chunk.load(mapType);
		return chunk;
	}

	void load(MapType chunkData) {
		ListType<MapType> sectionsData = chunkData.getList("Sections", new ListType<>());
		int y = 0;
		for (MapType sectionData : sectionsData) {
			this.sections[y].dispose();
			this.sections[y] = new Section(new GridPoint3(this.offset.x, this.offset.y + y * this.size, this.offset.z), sectionData);
			y++;
		}
	}

	public MapType save() {
		MapType chunkData = new MapType();
		ListType<MapType> sectionsData = new ListType<>();
		for (Section section : this.sections) {
			sectionsData.add(section.save());
		}
		chunkData.put("Sections", sectionsData);
		return chunkData;
	}

	public Block get(GridPoint3 pos) {
		return get(pos.x, pos.y, pos.z);
	}

	public Block get(int x, int y, int z) {
		if (x < 0 || x >= size) return Blocks.AIR;
		if (y < 0 || y >= height) return Blocks.AIR;
		if (z < 0 || z >= size) return Blocks.AIR;
		return getFast(x, y, z);
	}

	public Block getFast(GridPoint3 pos) {
		return getFast(pos.x, pos.y, pos.z);
	}

	public Block getFast(int x, int y, int z) {
		synchronized (this.lock) {
			return this.sections[y / this.size].getFast(x, y % this.size, z);
		}
	}

	public void set(GridPoint3 pos, Block block) {
		set(pos.x, pos.y, pos.z, block);
	}

	public void set(int x, int y, int z, Block block) {
		if (x < 0 || x >= size) return;
		if (y < 0 || y >= height) return;
		if (z < 0 || z >= size) return;
		setFast(x, y, z, block);
	}

	public void setFast(GridPoint3 pos, Block block) {
		set(pos.x, pos.y, pos.z, block);
	}

	public void setFast(int x, int y, int z, Block block) {
		synchronized (this.lock) {
			this.sections[y / this.size].setFast(x, y % this.size, z, block);
			this.dirty = true;
		}
		updateNeighbours = true;
	}

	public Section getSection(int sectionY) {
		synchronized (this.lock) {
			if (sectionY < 0 || sectionY > this.sections.length) return null;
			return this.sections[sectionY];
		}
	}

	public Section getSectionAt(int chunkY) {
		return this.getSection(chunkY / this.size);
	}

	private GridPoint3 reverse(int index) {
		int y = index / sizeTimesHeight;
		int z = (index - y * sizeTimesHeight) / size;
		int x = index - y * sizeTimesHeight - z * size;
		return new GridPoint3(x, y, z);
	}

	@Override
	public void dispose() {
		synchronized (this.lock) {
			this.ready = false;

			Section[] sections = this.sections;
			for (Section section : sections) {
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
		return List.of(this.sections);
	}

	public GridPoint3 getOffset() {
		return this.offset.cpy();
	}

	public World getWorld() {
		return world;
	}
}
package com.ultreon.craft.world;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.Task;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.world.gen.TreeData;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;

import it.unimi.dsi.fastutil.floats.FloatList;

public class Chunk implements Disposable {
	public static final int VERTEX_SIZE = 6;
	public final ChunkPos pos;
	protected final Object lock = new Object();
	protected boolean modifiedByPlayer;
	protected boolean ready;
	private Section[] sections;
	public final int size;
	public final int height;
	public final GridPoint3 offset = new GridPoint3();
	private final int sizeTimesHeight;
	private final int topOffset;
	private final int bottomOffset;
	private final int leftOffset;
	private final int rightOffset;
	private final int frontOffset;
	private final int backOffset;
	public TreeData treeData;
	protected Mesh mesh;
	protected Material material;
	protected boolean dirty;

	protected int numVertices;
	private final World world;

	public Chunk(World world, int size, int height, ChunkPos pos) {
		int sectionCount = height / size;

		this.world = world;
		this.pos = pos;
		this.sections = new Section[sectionCount];
		this.size = size;
		this.height = height;
		this.topOffset = size * size;
		this.bottomOffset = -size * size;
		this.leftOffset = -1;
		this.rightOffset = 1;
		this.frontOffset = -size;
		this.backOffset = size;
		this.sizeTimesHeight = size * size;

		for (int i = 0; i < this.sections.length; i++) {
			this.sections[i] = new Section();
		}
	}

	public static Chunk load(World world, ChunkPos pos, MapType mapType) {
		Chunk chunk = new Chunk(world, World.CHUNK_SIZE, World.CHUNK_HEIGHT, pos);
		chunk.load(mapType);
		return chunk;
	}

	void load(MapType chunkData) {
		ListType<MapType> sectionsData = chunkData.getList("Sections", new ListType<>());
		int i = 0;
		for (MapType sectionData : sectionsData) {
			this.sections[i].dispose();
			this.sections[i] = new Section(sectionData);
			i++;
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

	/** Creates a mesh out of the chunk, returning the number of indices produced
	 * @return the number of vertices produced */
	public int calculateVertices(FloatList vertices) {
		int i = 0;
		for (int y = 0; y < height; y++) {
			for (int z = 0; z < size; z++) {
				for (int x = 0; x < size; x++, i++) {
					Block block = get(x, y, z);

					if (block == null || block == Blocks.AIR) continue;

					BakedCubeModel model = UltreonCraft.get().getBakedBlockModel(block);

					if (model == null) continue;

					if (y < height - 1) {
						if (getB(x, y + 1, z) == null || getB(x, y + 1, z) == Blocks.AIR || getB(x, y + 1, z).isTransparent()) createTop(offset, x, y, z, model.top(), vertices);
					} else {
						createTop(offset, x, y, z, model.top(), vertices);
					}
					if (y > 0) {
						if (getB(x, y - 1, z) == null || getB(x, y - 1, z) == Blocks.AIR || getB(x, y - 1, z).isTransparent()) createBottom(offset, x, y, z, model.bottom(), vertices);
					} else {
						createBottom(offset, x, y, z, model.bottom(), vertices);
					}
					if (x > 0) {
						if (getB(x - 1, y, z) == null || getB(x - 1, y, z) == Blocks.AIR || getB(x - 1, y, z).isTransparent()) createLeft(offset, x, y, z, model.left(), vertices);
					} else {
						createLeft(offset, x, y, z, model.left(), vertices);
					}
					if (x < size - 1) {
						if (getB(x + 1, y, z) == null || getB(x + 1, y, z) == Blocks.AIR || getB(x + 1, y, z).isTransparent()) createRight(offset, x, y, z, model.right(), vertices);
					} else {
						createRight(offset, x, y, z, model.right(), vertices);
					}
					if (z > 0) {
						if (getB(x, y, z - 1) == null || getB(x, y, z - 1) == Blocks.AIR || getB(x, y, z - 1).isTransparent()) createFront(offset, x, y, z, model.front(), vertices);
					} else {
						createFront(offset, x, y, z, model.front(), vertices);
					}
					if (z < size - 1) {
						if (getB(x, y, z + 1) == null || getB(x, y, z + 1) == Blocks.AIR || getB(x, y, z + 1).isTransparent()) createBack(offset, x, y, z, model.back(), vertices);
					} else {
						createBack(offset, x, y, z, model.back(), vertices);
					}
				}
			}
		}
		return vertices.size() / VERTEX_SIZE + 1;
	}

	private Block getB(int x, int y, int z) {
//		return world.get(new GridPoint3(pos.x * size + x, y, pos.z * size + z));
		return get(new GridPoint3(x, y, z));
	}

	public static void createTop(GridPoint3 offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
		vertices.add(offset.x + x);
		vertices.add(offset.y + y + 1);
		vertices.add(offset.z + z);
		vertices.add(0);
		vertices.add(1);
		vertices.add(0);
		vertices.add(region.getU());
		vertices.add(region.getV());

		vertices.add(offset.x + x + 1);
		vertices.add(offset.y + y + 1);
		vertices.add(offset.z + z);
		vertices.add(0);
		vertices.add(1);
		vertices.add(0);
		vertices.add(region.getU2());
		vertices.add(region.getV());

		vertices.add(offset.x + x + 1);
		vertices.add(offset.y + y + 1);
		vertices.add(offset.z + z + 1);
		vertices.add(0);
		vertices.add(1);
		vertices.add(0);
		vertices.add(region.getU2());
		vertices.add(region.getV2());

		vertices.add(offset.x + x);
		vertices.add(offset.y + y + 1);
		vertices.add(offset.z + z + 1);
		vertices.add(0);
		vertices.add(1);
		vertices.add(0);
		vertices.add(region.getU());
		vertices.add(region.getV2());
	}

	public static void createBottom(GridPoint3 offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
		vertices.add(offset.x + x);
		vertices.add(offset.y + y);
		vertices.add(offset.z + z);
		vertices.add(0);
		vertices.add(-1);
		vertices.add(0);
		vertices.add(region.getU());
		vertices.add(region.getV());

		vertices.add(offset.x + x);
		vertices.add(offset.y + y);
		vertices.add(offset.z + z + 1);
		vertices.add(0);
		vertices.add(-1);
		vertices.add(0);
		vertices.add(region.getU());
		vertices.add(region.getV2());

		vertices.add(offset.x + x + 1);
		vertices.add(offset.y + y);
		vertices.add(offset.z + z + 1);
		vertices.add(0);
		vertices.add(-1);
		vertices.add(0);
		vertices.add(region.getU2());
		vertices.add(region.getV2());

		vertices.add(offset.x + x + 1);
		vertices.add(offset.y + y);
		vertices.add(offset.z + z);
		vertices.add(0);
		vertices.add(-1);
		vertices.add(0);
		vertices.add(region.getU2());
		vertices.add(region.getV());
	}

	public static void createLeft(GridPoint3 offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
		vertices.add(offset.x + x);
		vertices.add(offset.y + y);
		vertices.add(offset.z + z);
		vertices.add(-1);
		vertices.add(0);
		vertices.add(0);
		vertices.add(region.getU2());
		vertices.add(region.getV2());

		vertices.add(offset.x + x);
		vertices.add(offset.y + y + 1);
		vertices.add(offset.z + z);
		vertices.add(-1);
		vertices.add(0);
		vertices.add(0);
		vertices.add(region.getU2());
		vertices.add(region.getV());

		vertices.add(offset.x + x);
		vertices.add(offset.y + y + 1);
		vertices.add(offset.z + z + 1);
		vertices.add(-1);
		vertices.add(0);
		vertices.add(0);
		vertices.add(region.getU());
		vertices.add(region.getV());

		vertices.add(offset.x + x);
		vertices.add(offset.y + y);
		vertices.add(offset.z + z + 1);
		vertices.add(-1);
		vertices.add(0);
		vertices.add(0);
		vertices.add(region.getU());
		vertices.add(region.getV2());
	}

	public static void createRight(GridPoint3 offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
		vertices.add(offset.x + x + 1);
		vertices.add(offset.y + y);
		vertices.add(offset.z + z);
		vertices.add(1);
		vertices.add(0);
		vertices.add(0);
		vertices.add(region.getU2());
		vertices.add(region.getV2());

		vertices.add(offset.x + x + 1);
		vertices.add(offset.y + y);
		vertices.add(offset.z + z + 1);
		vertices.add(1);
		vertices.add(0);
		vertices.add(0);
		vertices.add(region.getU());
		vertices.add(region.getV2());

		vertices.add(offset.x + x + 1);
		vertices.add(offset.y + y + 1);
		vertices.add(offset.z + z + 1);
		vertices.add(1);
		vertices.add(0);
		vertices.add(0);
		vertices.add(region.getU());
		vertices.add(region.getV());

		vertices.add(offset.x + x + 1);
		vertices.add(offset.y + y + 1);
		vertices.add(offset.z + z);
		vertices.add(1);
		vertices.add(0);
		vertices.add(0);
		vertices.add(region.getU2());
		vertices.add(region.getV());
	}

	public static void createFront(GridPoint3 offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
		vertices.add(offset.x + x);
		vertices.add(offset.y + y);
		vertices.add(offset.z + z);
		vertices.add(0);
		vertices.add(0);
		vertices.add(1);
		vertices.add(region.getU2());
		vertices.add(region.getV2());

		vertices.add(offset.x + x + 1);
		vertices.add(offset.y + y);
		vertices.add(offset.z + z);
		vertices.add(0);
		vertices.add(0);
		vertices.add(1);
		vertices.add(region.getU());
		vertices.add(region.getV2());

		vertices.add(offset.x + x + 1);
		vertices.add(offset.y + y + 1);
		vertices.add(offset.z + z);
		vertices.add(0);
		vertices.add(0);
		vertices.add(1);
		vertices.add(region.getU());
		vertices.add(region.getV());

		vertices.add(offset.x + x);
		vertices.add(offset.y + y + 1);
		vertices.add(offset.z + z);
		vertices.add(0);
		vertices.add(0);
		vertices.add(1);
		vertices.add(region.getU2());
		vertices.add(region.getV());
	}

	public static void createBack(GridPoint3 offset, int x, int y, int z, TextureRegion region, FloatList vertices) {
		vertices.add(offset.x + x);
		vertices.add(offset.y + y);
		vertices.add(offset.z + z + 1);
		vertices.add(0);
		vertices.add(0);
		vertices.add(-1);
		vertices.add(region.getU2());
		vertices.add(region.getV2());

		vertices.add(offset.x + x);
		vertices.add(offset.y + y + 1);
		vertices.add(offset.z + z + 1);
		vertices.add(0);
		vertices.add(0);
		vertices.add(-1);
		vertices.add(region.getU2());
		vertices.add(region.getV());

		vertices.add(offset.x + x + 1);
		vertices.add(offset.y + y + 1);
		vertices.add(offset.z + z + 1);
		vertices.add(0);
		vertices.add(0);
		vertices.add(-1);
		vertices.add(region.getU());
		vertices.add(region.getV());

		vertices.add(offset.x + x + 1);
		vertices.add(offset.y + y);
		vertices.add(offset.z + z + 1);
		vertices.add(0);
		vertices.add(0);
		vertices.add(-1);
		vertices.add(region.getU());
		vertices.add(region.getV2());
	}

	@Override
	public void dispose() {
		synchronized (this.lock) {
			this.ready = false;

			if (this.mesh != null) {
				UltreonCraft.get().runLater(new Task(new Identifier("mesh_disposal"), this.mesh::dispose));
			}
			Section[] sections = this.sections;
			for (Section section : sections) {
				section.dispose();
			}
			this.material = null;
			this.sections = null;
			this.mesh = null;
		}
	}

	@Override
	public String toString() {
		return "Chunk[x=" + this.pos.x() + ", z=" + this.pos.z() + "]";
	}
}
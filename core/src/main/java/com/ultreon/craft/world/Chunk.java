package com.ultreon.craft.world;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.world.gen.TreeData;
import it.unimi.dsi.fastutil.objects.Reference2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;

import java.util.ArrayList;
import java.util.List;

public class Chunk {
	public static final int VERTEX_SIZE = 6;
	public static final List<TextureRegion> BREAK_TEX = new ArrayList<>();
	private final Reference2FloatMap<GridPoint3> breaking = new Reference2FloatArrayMap<>();
	public final ChunkPos pos;
	protected final Object lock = new Object();
	protected boolean modifiedByPlayer;
	protected boolean ready;
	private Block[] blocks;
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
	protected Mesh transparentMesh;
	protected Material material;
	protected Material transparentMaterial;
	protected boolean dirty;

	protected int numVertices;
	protected int numTransparentVertices;
	private final World world;

	public Chunk(World world, int size, int height, ChunkPos pos) {
		this.world = world;
		this.pos = pos;
		this.blocks = new Block[size * height * size];
		this.size = size;
		this.height = height;
		this.topOffset = size * size;
		this.bottomOffset = -size * size;
		this.leftOffset = -1;
		this.rightOffset = 1;
		this.frontOffset = -size;
		this.backOffset = size;
		this.sizeTimesHeight = size * size;
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
		return blocks[x + z * size + y * sizeTimesHeight];
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
		blocks[x + z * size + y * sizeTimesHeight] = block;
	}

	private GridPoint3 reverse(int index) {
		int y = index / sizeTimesHeight;
		int z = (index - y * sizeTimesHeight) / size;
		int x = index - y * sizeTimesHeight - z * size;
		return new GridPoint3(x, y, z);
	}

	/** Creates a mesh out of the chunk, returning the number of indices produced
	 * @return the number of vertices produced */
	public int calculateVertices(float[] vertices) {
		int i = 0;
		int vertexOffset = 0;
		for (int y = 0; y < height; y++) {
			for (int z = 0; z < size; z++) {
				for (int x = 0; x < size; x++, i++) {
					Block block = get(x, y, z);

					if (block == null || block == Blocks.AIR) continue;
					if (block.isTransparent()) continue;

					BakedCubeModel model = block.bakedModel();

					if (model == null) continue;

					if (y < height - 1) {
						if (getB(x, y + 1, z) == null || getB(x, y + 1, z) == Blocks.AIR || getB(x, y + 1, z).isTransparent()) vertexOffset = createTop(offset, x, y, z, model.top(), vertices, vertexOffset);
					} else {
						vertexOffset = createTop(offset, x, y, z, model.top(), vertices, vertexOffset);
					}
					if (y > 0) {
						if (getB(x, y - 1, z) == null || getB(x, y - 1, z) == Blocks.AIR || getB(x, y - 1, z).isTransparent()) vertexOffset = createBottom(offset, x, y, z, model.bottom(), vertices, vertexOffset);
					} else {
						vertexOffset = createBottom(offset, x, y, z, model.bottom(), vertices, vertexOffset);
					}
					if (x > 0) {
						if (getB(x - 1, y, z) == null || getB(x - 1, y, z) == Blocks.AIR || getB(x - 1, y, z).isTransparent()) vertexOffset = createLeft(offset, x, y, z, model.left(), vertices, vertexOffset);
					} else {
						vertexOffset = createLeft(offset, x, y, z, model.left(), vertices, vertexOffset);
					}
					if (x < size - 1) {
						if (getB(x + 1, y, z) == null || getB(x + 1, y, z) == Blocks.AIR || getB(x + 1, y, z).isTransparent()) vertexOffset = createRight(offset, x, y, z, model.right(), vertices, vertexOffset);
					} else {
						vertexOffset = createRight(offset, x, y, z, model.right(), vertices, vertexOffset);
					}
					if (z > 0) {
						if (getB(x, y, z - 1) == null || getB(x, y, z - 1) == Blocks.AIR || getB(x, y, z - 1).isTransparent()) vertexOffset = createFront(offset, x, y, z, model.front(), vertices, vertexOffset);
					} else {
						vertexOffset = createFront(offset, x, y, z, model.front(), vertices, vertexOffset);
					}
					if (z < size - 1) {
						if (getB(x, y, z + 1) == null || getB(x, y, z + 1) == Blocks.AIR || getB(x, y, z + 1).isTransparent()) vertexOffset = createBack(offset, x, y, z, model.back(), vertices, vertexOffset);
					} else {
						vertexOffset = createBack(offset, x, y, z, model.back(), vertices, vertexOffset);
					}
				}
			}
		}
		return vertexOffset / VERTEX_SIZE + 1;
	}

	private Block getB(int x, int y, int z) {
//		return world.get(new GridPoint3(pos.x * size + x, y, pos.z * size + z));
		return get(new GridPoint3(x, y, z));
	}

	/** Creates a mesh out of the chunk, returning the number of indices produced
	 * @return the number of vertices produced */
	public int calculateTransparentVertices(float[] vertices) {
		int i = 0;
		int vertexOffset = 0;
		for (int y = 0; y < height; y++) {
			for (int z = 0; z < size; z++) {
				for (int x = 0; x < size; x++, i++) {
					Block block = blocks[i];

					if (block == null || block == Blocks.AIR) continue;
					if (!block.isTransparent()) continue;

					BakedCubeModel model = block.bakedModel();

					if (model == null) continue;

					if (y < height - 1) {
						if (getB(x, y + 1, z) == null || getB(x, y + 1, z) == Blocks.AIR) vertexOffset = createTop(offset, x, y, z, model.top(), vertices, vertexOffset);
					} else {
//						vertexOffset = createTop(offset, x, y, z, model.top(), vertices, vertexOffset);
					}
					if (y > 0) {
						if (getB(x, y - 1, z) == null || getB(x, y - 1, z) == Blocks.AIR) vertexOffset = createBottom(offset, x, y, z, model.bottom(), vertices, vertexOffset);
					} else {
//						vertexOffset = createBottom(offset, x, y, z, model.bottom(), vertices, vertexOffset);
					}
					if (x > 0) {
						if (getB(x - 1, y, z) == null || getB(x - 1, y, z) == Blocks.AIR) vertexOffset = createLeft(offset, x, y, z, model.left(), vertices, vertexOffset);
					} else {
//						vertexOffset = createLeft(offset, x, y, z, model.left(), vertices, vertexOffset);
					}
					if (x < size - 1) {
						if (getB(x + 1, y, z) == null || getB(x + 1, y, z) == Blocks.AIR) vertexOffset = createRight(offset, x, y, z, model.right(), vertices, vertexOffset);
					} else {
//						vertexOffset = createRight(offset, x, y, z, model.right(), vertices, vertexOffset);
					}
					if (z > 0) {
						if (getB(x, y, z - 1) == null || getB(x, y, z - 1) == Blocks.AIR) vertexOffset = createFront(offset, x, y, z, model.front(), vertices, vertexOffset);
					} else {
//						vertexOffset = createFront(offset, x, y, z, model.front(), vertices, vertexOffset);
					}
					if (z < size - 1) {
						if (getB(x, y, z + 1) == null || getB(x, y, z + 1) == Blocks.AIR) vertexOffset = createBack(offset, x, y, z, model.back(), vertices, vertexOffset);
					} else {
//						vertexOffset = createBack(offset, x, y, z, model.back(), vertices, vertexOffset);
					}
				}
			}
		}
		return vertexOffset / VERTEX_SIZE + 1;
	}

	private static TextureRegion getBreakTex(float progress) {
		return BREAK_TEX.get((int) (BREAK_TEX.size() * progress));
	}

	private float getBreakProgress(float x, float y, float z) {
		GridPoint3 pos = new GridPoint3((int) x, (int) y, (int) z);
		if (breaking.containsKey(pos)) {
			return breaking.getFloat(pos);
		}
		return 0.0F;
	}

	public int createTop(GridPoint3 offset, float x, float y, float z, TextureRegion region, float[] vertices, int vertexOffset) {
		vertices[vertexOffset++] = offset.x + x;
		vertices[vertexOffset++] = offset.y + y + 1;
		vertices[vertexOffset++] = offset.z + z;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU();
		vertices[vertexOffset++] = region.getV();

		vertices[vertexOffset++] = offset.x + x + 1;
		vertices[vertexOffset++] = offset.y + y + 1;
		vertices[vertexOffset++] = offset.z + z;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU2();
		vertices[vertexOffset++] = region.getV();

		vertices[vertexOffset++] = offset.x + x + 1;
		vertices[vertexOffset++] = offset.y + y + 1;
		vertices[vertexOffset++] = offset.z + z + 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU2();
		vertices[vertexOffset++] = region.getV2();

		vertices[vertexOffset++] = offset.x + x;
		vertices[vertexOffset++] = offset.y + y + 1;
		vertices[vertexOffset++] = offset.z + z + 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU();
		vertices[vertexOffset++] = region.getV2();
		return vertexOffset;
	}

	public int createBottom(GridPoint3 offset, int x, int y, int z, TextureRegion region, float[] vertices, int vertexOffset) {
		vertices[vertexOffset++] = offset.x + x;
		vertices[vertexOffset++] = offset.y + y;
		vertices[vertexOffset++] = offset.z + z;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = -1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU();
		vertices[vertexOffset++] = region.getV();

		vertices[vertexOffset++] = offset.x + x;
		vertices[vertexOffset++] = offset.y + y;
		vertices[vertexOffset++] = offset.z + z + 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = -1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU();
		vertices[vertexOffset++] = region.getV2();

		vertices[vertexOffset++] = offset.x + x + 1;
		vertices[vertexOffset++] = offset.y + y;
		vertices[vertexOffset++] = offset.z + z + 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = -1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU2();
		vertices[vertexOffset++] = region.getV2();

		vertices[vertexOffset++] = offset.x + x + 1;
		vertices[vertexOffset++] = offset.y + y;
		vertices[vertexOffset++] = offset.z + z;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = -1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU2();
		vertices[vertexOffset++] = region.getV();
		return vertexOffset;
	}

	public int createLeft(GridPoint3 offset, int x, int y, int z, TextureRegion region, float[] vertices, int vertexOffset) {
		vertices[vertexOffset++] = offset.x + x;
		vertices[vertexOffset++] = offset.y + y;
		vertices[vertexOffset++] = offset.z + z;
		vertices[vertexOffset++] = -1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU2();
		vertices[vertexOffset++] = region.getV2();

		vertices[vertexOffset++] = offset.x + x;
		vertices[vertexOffset++] = offset.y + y + 1;
		vertices[vertexOffset++] = offset.z + z;
		vertices[vertexOffset++] = -1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU2();
		vertices[vertexOffset++] = region.getV();

		vertices[vertexOffset++] = offset.x + x;
		vertices[vertexOffset++] = offset.y + y + 1;
		vertices[vertexOffset++] = offset.z + z + 1;
		vertices[vertexOffset++] = -1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU();
		vertices[vertexOffset++] = region.getV();

		vertices[vertexOffset++] = offset.x + x;
		vertices[vertexOffset++] = offset.y + y;
		vertices[vertexOffset++] = offset.z + z + 1;
		vertices[vertexOffset++] = -1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU();
		vertices[vertexOffset++] = region.getV2();
		return vertexOffset;
	}

	public int createRight(GridPoint3 offset, float x, float y, float z, TextureRegion region, float[] vertices, int vertexOffset) {
		vertices[vertexOffset++] = offset.x + x + 1;
		vertices[vertexOffset++] = offset.y + y;
		vertices[vertexOffset++] = offset.z + z;
		vertices[vertexOffset++] = 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU2();
		vertices[vertexOffset++] = region.getV2();

		vertices[vertexOffset++] = offset.x + x + 1;
		vertices[vertexOffset++] = offset.y + y;
		vertices[vertexOffset++] = offset.z + z + 1;
		vertices[vertexOffset++] = 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU();
		vertices[vertexOffset++] = region.getV2();

		vertices[vertexOffset++] = offset.x + x + 1;
		vertices[vertexOffset++] = offset.y + y + 1;
		vertices[vertexOffset++] = offset.z + z + 1;
		vertices[vertexOffset++] = 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU();
		vertices[vertexOffset++] = region.getV();

		vertices[vertexOffset++] = offset.x + x + 1;
		vertices[vertexOffset++] = offset.y + y + 1;
		vertices[vertexOffset++] = offset.z + z;
		vertices[vertexOffset++] = 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = region.getU2();
		vertices[vertexOffset++] = region.getV();
		return vertexOffset;
	}

	public int createFront(GridPoint3 offset, float x, float y, float z, TextureRegion region, float[] vertices, int vertexOffset) {
		vertices[vertexOffset++] = offset.x + x;
		vertices[vertexOffset++] = offset.y + y;
		vertices[vertexOffset++] = offset.z + z;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 1;
		vertices[vertexOffset++] = region.getU2();
		vertices[vertexOffset++] = region.getV2();

		vertices[vertexOffset++] = offset.x + x + 1;
		vertices[vertexOffset++] = offset.y + y;
		vertices[vertexOffset++] = offset.z + z;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 1;
		vertices[vertexOffset++] = region.getU();
		vertices[vertexOffset++] = region.getV2();

		vertices[vertexOffset++] = offset.x + x + 1;
		vertices[vertexOffset++] = offset.y + y + 1;
		vertices[vertexOffset++] = offset.z + z;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 1;
		vertices[vertexOffset++] = region.getU();
		vertices[vertexOffset++] = region.getV();

		vertices[vertexOffset++] = offset.x + x;
		vertices[vertexOffset++] = offset.y + y + 1;
		vertices[vertexOffset++] = offset.z + z;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 1;
		vertices[vertexOffset++] = region.getU2();
		vertices[vertexOffset++] = region.getV();
		return vertexOffset;
	}

	public int createBack(GridPoint3 offset, float x, float y, float z, TextureRegion region, float[] vertices, int vertexOffset) {
		vertices[vertexOffset++] = offset.x + x;
		vertices[vertexOffset++] = offset.y + y;
		vertices[vertexOffset++] = offset.z + z + 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = -1;
		vertices[vertexOffset++] = region.getU2();
		vertices[vertexOffset++] = region.getV2();

		vertices[vertexOffset++] = offset.x + x;
		vertices[vertexOffset++] = offset.y + y + 1;
		vertices[vertexOffset++] = offset.z + z + 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = -1;
		vertices[vertexOffset++] = region.getU2();
		vertices[vertexOffset++] = region.getV();

		vertices[vertexOffset++] = offset.x + x + 1;
		vertices[vertexOffset++] = offset.y + y + 1;
		vertices[vertexOffset++] = offset.z + z + 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = -1;
		vertices[vertexOffset++] = region.getU();
		vertices[vertexOffset++] = region.getV();

		vertices[vertexOffset++] = offset.x + x + 1;
		vertices[vertexOffset++] = offset.y + y;
		vertices[vertexOffset++] = offset.z + z + 1;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = 0;
		vertices[vertexOffset++] = -1;
		vertices[vertexOffset++] = region.getU();
		vertices[vertexOffset++] = region.getV2();
		return vertexOffset;
	}

	public void dispose() {
		UltreonCraft.get().runLater(this.mesh::dispose);
		UltreonCraft.get().runLater(this.transparentMesh::dispose);
		this.material = null;
		this.blocks = null;
		this.mesh = null;
		this.transparentMesh = null;
	}

	@Override
	public String toString() {
		return "Chunk[x=" + pos.x + ", z=" + pos.z + "]";
	}
}
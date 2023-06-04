package com.ultreon.craft.world;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.GridPoint3;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.render.model.BakedCubeModel;

public class Chunk extends RawChunk {
	public static final int VERTEX_SIZE = 6;
	protected boolean ready;
	protected Mesh mesh;
	protected Material material;
	protected boolean dirty;

	protected int numVertices;

	public Chunk(World world, int size, int height, ChunkPos pos) {
		this(world, size, height, pos, new Block[size * height * size], new Heightmap());
	}

	Chunk(World world, int size, int height, ChunkPos pos, Block[] blocks, Heightmap heightmap) {
		super(world, size, height, pos, blocks, heightmap);
	}

	@Override
	public void setFast(int x, int y, int z, Block block) {
		super.setFast(x, y, z, block);
		this.dirty = true;
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

					BakedCubeModel model = UltreonCraft.get().getBakedBlockModel(block);

					if (model == null) model = BakedCubeModel.DEFAULT;

					boolean alwaysRenderFaces = block.getAlwaysRenderFaces();
					if (alwaysRenderFaces) {
						vertexOffset = createTop(offset, x, y, z, model.top(), vertices, vertexOffset);
						vertexOffset = createBottom(offset, x, y, z, model.top(), vertices, vertexOffset);
						vertexOffset = createLeft(offset, x, y, z, model.top(), vertices, vertexOffset);
						vertexOffset = createRight(offset, x, y, z, model.top(), vertices, vertexOffset);
						vertexOffset = createFront(offset, x, y, z, model.top(), vertices, vertexOffset);
						vertexOffset = createBack(offset, x, y, z, model.top(), vertices, vertexOffset);
					} else {
						if (y < height - 1) {
							if (getB(x, y + 1, z) == null || getB(x, y + 1, z) == Blocks.AIR || getB(x, y + 1, z).isTransparent()) {
								vertexOffset = createTop(offset, x, y, z, model.top(), vertices, vertexOffset);
							}
						} else {
							vertexOffset = createTop(offset, x, y, z, model.top(), vertices, vertexOffset);
						}
						if (y > 0) {
							if (getB(x, y - 1, z) == null || getB(x, y - 1, z) == Blocks.AIR || getB(x, y - 1, z).isTransparent())
								vertexOffset = createBottom(offset, x, y, z, model.bottom(), vertices, vertexOffset);
						} else {
							vertexOffset = createBottom(offset, x, y, z, model.bottom(), vertices, vertexOffset);
						}
						if (x > 0) {
							if (getB(x - 1, y, z) == null || getB(x - 1, y, z) == Blocks.AIR || getB(x - 1, y, z).isTransparent())
								vertexOffset = createLeft(offset, x, y, z, model.left(), vertices, vertexOffset);
						} else {
							vertexOffset = createLeft(offset, x, y, z, model.left(), vertices, vertexOffset);
						}
						if (x < size - 1) {
							if (getB(x + 1, y, z) == null || getB(x + 1, y, z) == Blocks.AIR || getB(x + 1, y, z).isTransparent())
								vertexOffset = createRight(offset, x, y, z, model.right(), vertices, vertexOffset);
						} else {
							vertexOffset = createRight(offset, x, y, z, model.right(), vertices, vertexOffset);
						}
						if (z > 0) {
							if (getB(x, y, z - 1) == null || getB(x, y, z - 1) == Blocks.AIR || getB(x, y, z - 1).isTransparent())
								vertexOffset = createFront(offset, x, y, z, model.front(), vertices, vertexOffset);
						} else {
							vertexOffset = createFront(offset, x, y, z, model.front(), vertices, vertexOffset);
						}
						if (z < size - 1) {
							if (getB(x, y, z + 1) == null || getB(x, y, z + 1) == Blocks.AIR || getB(x, y, z + 1).isTransparent())
								vertexOffset = createBack(offset, x, y, z, model.back(), vertices, vertexOffset);
						} else {
							vertexOffset = createBack(offset, x, y, z, model.back(), vertices, vertexOffset);
						}
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

	public static int createTop(GridPoint3 offset, int x, int y, int z, TextureRegion region, float[] vertices, int vertexOffset) {
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

	public static int createBottom(GridPoint3 offset, int x, int y, int z, TextureRegion region, float[] vertices, int vertexOffset) {
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

	public static int createLeft(GridPoint3 offset, int x, int y, int z, TextureRegion region, float[] vertices, int vertexOffset) {
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

	public static int createRight(GridPoint3 offset, int x, int y, int z, TextureRegion region, float[] vertices, int vertexOffset) {
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

	public static int createFront(GridPoint3 offset, int x, int y, int z, TextureRegion region, float[] vertices, int vertexOffset) {
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

	public static int createBack(GridPoint3 offset, int x, int y, int z, TextureRegion region, float[] vertices, int vertexOffset) {
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

	@Override
	public void dispose() {
		this.ready = false;

		UltreonCraft.get().runLater(this.mesh::dispose);
		this.material = null;
		this.mesh = null;
	}

	@Override
	public String toString() {
		return "Chunk[x=" + pos.x + ", z=" + pos.z + "]";
	}

	public int getHeight(int x, int z) {
		return heightmap.get(x, z);
	}
}
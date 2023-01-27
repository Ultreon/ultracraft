/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *   http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.ultreon.craft.world;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.render.model.BakedCubeModel;

public class Chunk {
	public static final int VERTEX_SIZE = 6;
	public final Block[] blocks;
	public final int width;
	public final int height;
	public final int depth;
	public final Vector3 offset = new Vector3();
	private final int widthTimesHeight;
	private final int topOffset;
	private final int bottomOffset;
	private final int leftOffset;
	private final int rightOffset;
	private final int frontOffset;
	private final int backOffset;

	public Chunk(int width, int height, int depth) {
		this.blocks = new Block[width * height * depth];
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.topOffset = width * depth;
		this.bottomOffset = -width * depth;
		this.leftOffset = -1;
		this.rightOffset = 1;
		this.frontOffset = -width;
		this.backOffset = width;
		this.widthTimesHeight = width * height;
	}

	public Block get(BlockPos pos) {
		return get(pos.x(), pos.y(), pos.z());
	}

	public Block get(int x, int y, int z) {
		if (x < 0 || x >= width) return Blocks.AIR.get();
		if (y < 0 || y >= height) return Blocks.AIR.get();
		if (z < 0 || z >= depth) return Blocks.AIR.get();
		return getFast(x, y, z);
	}

	public Block getFast(BlockPos pos) {
		return getFast(pos.x(), pos.y(), pos.z());
	}

	public Block getFast(int x, int y, int z) {
		return blocks[x + z * width + y * widthTimesHeight];
	}

	public void set(BlockPos pos, Block block) {
		set(pos.x(), pos.y(), pos.z(), block);
	}

	public void set(int x, int y, int z, Block block) {
		if (x < 0 || x >= width) return;
		if (y < 0 || y >= height) return;
		if (z < 0 || z >= depth) return;
		setFast(x, y, z, block);
	}

	public void setFast(BlockPos pos, Block block) {
		set(pos.x(), pos.y(), pos.z(), block);
	}

	public void setFast(int x, int y, int z, Block block) {
		blocks[x + z * width + y * widthTimesHeight] = block;
	}

	/** Creates a mesh out of the chunk, returning the number of indices produced
	 * @return the number of vertices produced */
	public int calculateVertices(float[] vertices) {
		int i = 0;
		int vertexOffset = 0;
		for (int y = 0; y < height; y++) {
			for (int z = 0; z < depth; z++) {
				for (int x = 0; x < width; x++, i++) {
					Block block = blocks[i];
					if (block == null || block == Blocks.AIR.get()) continue;

					BakedCubeModel model = block.bakedModel();
					if (model == null) continue;

					if (y < height - 1) {
						if (blocks[i + topOffset] == null || blocks[i + topOffset] == Blocks.AIR.get()) vertexOffset = createTop(offset, x, y, z, model.top(), vertices, vertexOffset);
					} else {
						vertexOffset = createTop(offset, x, y, z, model.top(), vertices, vertexOffset);
					}
					if (y > 0) {
						if (blocks[i + bottomOffset] == null || blocks[i + bottomOffset] == Blocks.AIR.get()) vertexOffset = createBottom(offset, x, y, z, model.bottom(), vertices, vertexOffset);
					} else {
						vertexOffset = createBottom(offset, x, y, z, model.bottom(), vertices, vertexOffset);
					}
					if (x > 0) {
						if (blocks[i + leftOffset] == null || blocks[i + leftOffset] == Blocks.AIR.get()) vertexOffset = createLeft(offset, x, y, z, model.left(), vertices, vertexOffset);
					} else {
						vertexOffset = createLeft(offset, x, y, z, model.left(), vertices, vertexOffset);
					}
					if (x < width - 1) {
						if (blocks[i + rightOffset] == null || blocks[i + rightOffset] == Blocks.AIR.get()) vertexOffset = createRight(offset, x, y, z, model.right(), vertices, vertexOffset);
					} else {
						vertexOffset = createRight(offset, x, y, z, model.right(), vertices, vertexOffset);
					}
					if (z > 0) {
						if (blocks[i + frontOffset] == null || blocks[i + frontOffset] == Blocks.AIR.get()) vertexOffset = createFront(offset, x, y, z, model.front(), vertices, vertexOffset);
					} else {
						vertexOffset = createFront(offset, x, y, z, model.front(), vertices, vertexOffset);
					}
					if (z < depth - 1) {
						if (blocks[i + backOffset] == null || blocks[i + backOffset] == Blocks.AIR.get()) vertexOffset = createBack(offset, x, y, z, model.back(), vertices, vertexOffset);
					} else {
						vertexOffset = createBack(offset, x, y, z, model.back(), vertices, vertexOffset);
					}
				}
			}
		}
		return vertexOffset / VERTEX_SIZE;
	}

	public static int createTop(Vector3 offset, int x, int y, int z, TextureRegion region, float[] vertices, int vertexOffset) {

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

	public static int createBottom(Vector3 offset, int x, int y, int z, TextureRegion region, float[] vertices, int vertexOffset) {
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

	public static int createLeft(Vector3 offset, int x, int y, int z, TextureRegion region, float[] vertices, int vertexOffset) {
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

	public static int createRight(Vector3 offset, int x, int y, int z, TextureRegion region, float[] vertices, int vertexOffset) {
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

	public static int createFront(Vector3 offset, int x, int y, int z, TextureRegion region, float[] vertices, int vertexOffset) {
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

	public static int createBack(Vector3 offset, int x, int y, int z, TextureRegion region, float[] vertices, int vertexOffset) {
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
}
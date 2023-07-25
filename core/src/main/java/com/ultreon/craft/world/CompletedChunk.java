package com.ultreon.craft.world;

import com.badlogic.gdx.utils.Disposable;
import com.ultreon.craft.world.gen.TreeData;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.Nullable;

import static com.ultreon.craft.world.World.CHUNK_SIZE;

public class CompletedChunk extends Chunk implements Disposable {
	public static final int VERTEX_SIZE = 6;
	protected boolean ready;
	@Nullable
	public TreeData treeData;
	protected boolean dirty;

	public CompletedChunk(World world, int size, int height, ChunkPos pos) {
		super(world, size, height, pos);
	}

	public CompletedChunk(World world, int size, int height, ChunkPos pos, Heightmap heightmap) {
		super(world, size, height, pos, heightmap);
	}

	public static Chunk load(World world, ChunkPos pos, MapType mapType) {
		if (mapType.getBoolean("isBuilder")) {
			BuilderChunk chunk = new BuilderChunk(world, CHUNK_SIZE, World.CHUNK_HEIGHT, pos);
			chunk.load(mapType);
			return chunk;
		}
		CompletedChunk chunk = new CompletedChunk(world, CHUNK_SIZE, World.CHUNK_HEIGHT, pos);
		chunk.load(mapType);
		return chunk;
	}

	private boolean isOutOfBounds(int x, int y, int z) {
		return x < 0 || x >= this.size || y < 0 || y >= this.height || z < 0 || z >= this.size;
	}

	private Vec3i reverse(int index) {
		int y = index / this.sizeTimesHeight;
		int z = (index - y * this.sizeTimesHeight) / this.size;
		int x = index - y * this.sizeTimesHeight - z * this.size;
		return new Vec3i(x, y, z);
	}

	@Override
	public String toString() {
		return "Chunk[x=" + this.pos.x() + ", z=" + this.pos.z() + "]";
	}
}
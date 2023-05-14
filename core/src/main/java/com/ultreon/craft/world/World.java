package com.ultreon.craft.world;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.debug.Debugger;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.util.WorldRayCaster;
import com.ultreon.craft.world.gen.BiomeGenerator;
import com.ultreon.craft.world.gen.TerrainGenerator;
import com.ultreon.craft.world.gen.layer.*;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;
import com.ultreon.data.types.MapType;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class World implements RenderableProvider {
	public static final int CHUNK_SIZE = 16;
	public static final int CHUNK_HEIGHT = 256;
	public static final int WORLD_HEIGHT = 256;
	public static final float WORLD_DEPTH = 0;
	private final Texture texture;
	private final short[] indices;
	private float[] vertices;
	private boolean doRender = false;

	private final BiomeGenerator biome = BiomeGenerator.builder()
			.noise(NoiseSettingsInit.DOMAIN_X)
			.domainWarping(new DomainWarping(NoiseSettingsInit.DOMAIN_X, NoiseSettingsInit.DOMAIN_Y))
//			.layer(new WaterTerrainLayer(16))
			.layer(new AirTerrainLayer())
			.layer(new SurfaceTerrainLayer())
			.layer(new StoneTerrainLayer())
			.layer(new UndergroundTerrainLayer())
			.extraLayer(new StonePatchTerrainLayer(NoiseSettingsInit.STONE_PATCH, new DomainWarping(NoiseSettingsInit.DOMAIN_X, NoiseSettingsInit.DOMAIN_Y)))
			.build();
	private final long seed = 512;
	public final int chunksX;
	public final int chunksZ;
	public final int voxelsX;
	public final int voxelsZ;
	public int renderedChunks;
	public int numChunks;

	private final Map<ChunkPos, Chunk> chunks;
	private TerrainGenerator terrainGen;
	private final Int2ReferenceMap<Entity> entities = new Int2ReferenceArrayMap<>();
	private int playTime;
	private int curId;
	private final UltreonCraft game = UltreonCraft.get();

	public World(Texture texture, int chunksX, int chunksZ) {
		this.texture = texture;
		this.chunks = new ConcurrentHashMap<>();
		this.chunksX = chunksX;
		this.chunksZ = chunksZ;
//		this.numChunks = chunksX * chunksY * chunksZ;
		this.voxelsX = chunksX * CHUNK_SIZE;
		this.voxelsZ = chunksZ * CHUNK_SIZE;

		this.vertices = new float[Chunk.VERTEX_SIZE * 6 * CHUNK_SIZE * WORLD_HEIGHT * CHUNK_SIZE];

		int len = World.CHUNK_SIZE * World.CHUNK_HEIGHT * World.CHUNK_SIZE * 6 * 6 / 3;

		indices = new short[len];
		short j = 0;
		for (int i = 0; i < len; i += 6, j += 4) {
			indices[i] = j;
			indices[i + 1] = (short)(j + 1);
			indices[i + 2] = (short)(j + 2);
			indices[i + 3] = (short)(j + 2);
			indices[i + 4] = (short)(j + 3);
			indices[i + 5] = j;
		}
	}

	public HitResult rayCast(Ray ray) {
		return WorldRayCaster.rayCast(new HitResult(ray), this);
	}

	public void generateWorld() {
		for (int z = 0; z < chunksZ; z++) {
			for (int x = 0; x < chunksX; x++) {
				Chunk chunk = new Chunk(CHUNK_SIZE, CHUNK_HEIGHT);
				ChunkPos chunkPos = new ChunkPos(x, z);
				System.out.println("chunkPos = " + chunkPos);
				chunk.offset.set(x * CHUNK_SIZE, WORLD_DEPTH, z * CHUNK_SIZE);
				chunk.dirty = false;
				chunk.numVertices = 0;
				chunk.material = new Material(new TextureAttribute(TextureAttribute.Diffuse, texture));

				for (int bx = 0; bx < CHUNK_SIZE; bx++) {
					for (int by = 0; by < CHUNK_SIZE; by++) {
						biome.processColumn(chunk, chunkPos.x() * CHUNK_SIZE + bx, chunkPos.z() * CHUNK_SIZE + by, seed, CHUNK_HEIGHT);
					}
				}

				game.runLater(() -> {
					chunk.mesh = new Mesh(true, CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE * 6 * 4,
							CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE * 36 / 3, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
					chunk.mesh.setIndices(indices);
					chunk.ready = true;
					chunk.dirty = true;
				});
				putChunk(chunkPos, chunk);
			}
		}

		doRender = true;

		Debugger.dumpLayerInfo();
	}

	private void putChunk(ChunkPos chunkPos, Chunk chunk) {
		Chunk oldChunk = chunks.get(chunkPos);
		if (oldChunk != null) {
			oldChunk.dispose();
		}
		chunks.put(chunkPos, chunk);
	}

	public void tick() {
		playTime++;

		for (var entity : entities.values()) {
			entity.tick();
		}
	}

	public CompletableFuture<ConcurrentMap<Vector3, Chunk>> generateWorldChunkData(List<Vector3> toCreate) {
		ConcurrentMap<Vector3, Chunk> map = new ConcurrentHashMap<>();
		return CompletableFuture.supplyAsync(() -> {
			for (var pos : toCreate) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				Chunk chunk = new Chunk(CHUNK_SIZE, CHUNK_HEIGHT);
				Chunk newChunk = terrainGen.generateChunkData(chunk, seed);
			}
			return map;
		});
	}

	public void set(GridPoint3 blockPos, Block block) {
		set(blockPos.x, blockPos.y, blockPos.z, block);
	}

	public void set(int x, int y, int z, Block voxel) {
		Chunk chunk = getChunkAt(x, y, z);
		chunk.set(x % CHUNK_SIZE, y % CHUNK_HEIGHT, z % CHUNK_SIZE, voxel);
		chunk.dirty = true;
	}

	public Block get(GridPoint3 pos) {
		return get(pos.x, pos.y, pos.z);
	}

	@Nullable
	public Block get(int x, int y, int z) {
		Chunk chunkAt = getChunkAt(x, y, z);
		if (chunkAt == null) {
			return Blocks.AIR;
		}
		return chunkAt.get(x % CHUNK_SIZE, y % CHUNK_HEIGHT, z % CHUNK_SIZE);
	}

	public Chunk getChunk(ChunkPos chunkPos) {
		return chunks.get(chunkPos);
	}

	public Chunk getChunkAt(int x, int y, int z) {
		return getChunkAt(new GridPoint3(x, y, z));
	}

	public Chunk getChunkAt(GridPoint3 pos) {
		ChunkPos chunkPos = new ChunkPos(pos.x / CHUNK_SIZE, pos.z / CHUNK_SIZE);
		return getChunk(chunkPos);
	}

	public float getHighest(float x, float z) {
		int ix = (int)x;
		int iz = (int)z;
		if (ix < 0 || ix >= voxelsX) return 0;
		if (iz < 0 || iz >= voxelsZ) return 0;
		// FIXME optimize
		for (int y = CHUNK_HEIGHT - 1; y > 0; y--) {
			if (get(ix, y, iz) != Blocks.AIR) return y + 1;
		}
		return 0;
	}

	public void setColumn(int x,  int z, Block voxel) {
		setColumn(x, CHUNK_HEIGHT, z, voxel);
	}

	public void setColumn(int x, int y, int z, Block voxel) {
		if (getChunkAt(x, y, z) == null) return;

		// FIXME optimize
		for (; y > 0; y--) {
			set(x, y, z, voxel);
		}
	}

	// TODO: Port to new chunk system.
	@Deprecated
	public void setCube(int x, int y, int z, int width, int height, int depth, Block voxel) {
//		int ix = x;
//		int iy = y;
//		int iz = z;
//		int startX = Math.max(ix, 0);
//		int endX = Math.min(voxelsX, ix + width);
//		int startY = Math.max(iy, 0);
//		int endY = Math.min(voxelsY, iy + height);
//		int startZ = Math.max(iz, 0);
//		int endZ = Math.min(voxelsZ, iz + depth);
//		// FIXME optimize
//		for (iy = startY; iy < endY; iy++) {
//			for (iz = startZ; iz < endZ; iz++) {
//				for (ix = startX; ix < endX; ix++) {
//					set(ix, iy, iz, voxel);
//				}
//			}
//		}
	}

	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		if (!doRender) return;

		renderedChunks = 0;
		for (Chunk chunk : chunks.values()) {
			if (!chunk.ready) {
				System.out.println("chunk.ready = " + chunk.ready);
				continue;
			}

			Mesh mesh = chunk.mesh;
			if (chunk.dirty) {
				int numVertices = chunk.calculateVertices(this.vertices);
				chunk.numVertices = numVertices / 4 * 6;
				mesh.setVertices(this.vertices, 0, numVertices * Chunk.VERTEX_SIZE);
				chunk.dirty = false;
			}
			if (chunk.numVertices == 0) continue;
			Renderable renderable = pool.obtain();
			renderable.material = chunk.material;
			renderable.meshPart.mesh = mesh;
			renderable.meshPart.offset = 0;
			renderable.meshPart.size = chunk.numVertices;
			renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;
			renderables.add(renderable);
			renderedChunks++;
		}
	}

	public void regen() {
		int i = 0;
		for (int z = 0; z < chunksZ; z++) {
			for (int x = 0; x < chunksX; x++) {
				Chunk chunk = new Chunk(CHUNK_SIZE, CHUNK_HEIGHT);
				chunk.offset.set(x * CHUNK_SIZE, WORLD_DEPTH, z * CHUNK_SIZE);
				chunk.dirty = true;
				chunks.put(new ChunkPos(x, z), chunk);
			}
		}
		generateWorld();
	}

	public int getPlayTime() {
		return playTime;
	}

	public <T extends Entity> T spawn(T entity) {
		setEntityId(entity);
		entities.put(entity.getId(), entity);
		return entity;
	}

	public <T extends Entity> T spawn(T entity, MapType spawnData) {
		setEntityId(entity);
		entity.onPrepareSpawn(spawnData);
		entities.put(entity.getId(), entity);
		return entity;
	}

	private <T extends Entity> void setEntityId(T entity) {
		int oldId = entity.getId();
		if (oldId > 0 && entities.containsKey(oldId)) {
			throw new IllegalStateException("Entity already spawned: " + entity);
		}
		int newId = oldId > 0 ? oldId : nextId();
		entity.setId(newId);
	}

	private int nextId() {
		return curId++;
	}

	public void despawn(Entity entity) {
		entities.remove(entity.getId());
	}

	public void despawn(int id) {
		entities.remove(id);
	}

	public Entity getEntity(int id) {
		return entities.get(id);
	}

	public List<BoundingBox> collide(BoundingBox box) {
		List<BoundingBox> boxes = new ArrayList<>();
		int xMin = MathUtils.floor(box.min.x);
		int xMax = MathUtils.floor(box.max.x);
		int yMin = MathUtils.floor(box.min.y);
		int yMax = MathUtils.floor(box.max.y);
		int zMin = MathUtils.floor(box.min.z);
		int zMax = MathUtils.floor(box.max.z);

		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				for (int z = zMin; z <= zMax; z++) {
					Block block = this.get(x, y, z);
					if (block != null && block.isSolid()) {
						BoundingBox blockBox = block.getBoundingBox(x, y, z);
						if (blockBox != null && blockBox.intersects(box)) {
							boxes.add(blockBox);
						}
					}
				}
			}
		}

		return boxes;
	}

	public void dispose() {
		for (Chunk chunk : chunks.values()) {
			chunk.dispose();
		}
		vertices = null;
	}
}
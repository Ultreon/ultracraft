package com.ultreon.craft.world;

import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.debug.Debugger;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.util.EnumFacing;
import com.ultreon.craft.world.gen.BiomeGenerator;
import com.ultreon.craft.world.gen.TerrainGenerator;
import com.ultreon.craft.world.gen.layer.*;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;
import com.ultreon.data.types.MapType;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ultreon.craft.util.EnumFacing.AxisDirection;

public class World implements RenderableProvider {
	public static final int CHUNK_SIZE = 16;
	public static final int CHUNK_HEIGHT = 256;
	public static final int WORLD_HEIGHT = 256;

	public Chunk[] chunkArray;
	public final Mesh[] meshArray;
	public final Material[] materialArray;
	public final boolean[] dirty;
	public final int[] numVertices;
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
	public float[] vertices;
	public final int chunksX;
	public final int chunksY;
	public final int chunksZ;
	public final int voxelsX;
	public final int voxelsY;
	public final int voxelsZ;
	public int renderedChunks;
	public int numChunks;

	private Map<ChunkPos, Chunk> chunks;
	private Map<ChunkPos, Mesh> meshes;
	private Map<ChunkPos, Material> materials;
	private TerrainGenerator terrainGen;
	private final Int2ReferenceMap<Entity> entities = new Int2ReferenceArrayMap<>();
	private int playTime;
	private int curId;

	public World(Texture texture, int chunksX, int chunksY, int chunksZ) {
		this.chunkArray = new Chunk[chunksX * chunksY * chunksZ];
		this.chunksX = chunksX;
		this.chunksY = chunksY;
		this.chunksZ = chunksZ;
		this.numChunks = chunksX * chunksY * chunksZ;
		this.voxelsX = chunksX * CHUNK_SIZE;
		this.voxelsY = chunksY * CHUNK_HEIGHT;
		this.voxelsZ = chunksZ * CHUNK_SIZE;
		int i = 0;
		for (int y = 0; y < chunksY; y++) {
			for (int z = 0; z < chunksZ; z++) {
				for (int x = 0; x < chunksX; x++) {
					Chunk chunk = new Chunk(CHUNK_SIZE, CHUNK_HEIGHT);
					chunk.offset.set(x * CHUNK_SIZE, y * CHUNK_HEIGHT, z * CHUNK_SIZE);
					chunkArray[i++] = chunk;
				}
			}
		}
		int len = CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE * 6 * 6 / 3;
		short[] indices = new short[len];
		short j = 0;
		for (i = 0; i < len; i += 6, j += 4) {
			indices[i] = j;
			indices[i + 1] = (short)(j + 1);
			indices[i + 2] = (short)(j + 2);
			indices[i + 3] = (short)(j + 2);
			indices[i + 4] = (short)(j + 3);
			indices[i + 5] = j;
		}
		this.meshArray = new Mesh[chunksX * chunksY * chunksZ];
		for (i = 0; i < meshArray.length; i++) {
			meshArray[i] = new Mesh(true, CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE * 6 * 4,
				CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE * 36 / 3, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));
			meshArray[i].setIndices(indices);
		}
		this.dirty = new boolean[chunksX * chunksY * chunksZ];
		for (i = 0; i < dirty.length; i++)
			dirty[i] = true;

		this.numVertices = new int[chunksX * chunksY * chunksZ];
		for (i = 0; i < numVertices.length; i++)
			numVertices[i] = 0;

		this.vertices = new float[Chunk.VERTEX_SIZE * 6 * CHUNK_SIZE * WORLD_HEIGHT * CHUNK_SIZE];
		this.materialArray = new Material[chunksX * chunksY * chunksZ];
		for (i = 0; i < materialArray.length; i++) {
			materialArray[i] = new Material(new TextureAttribute(TextureAttribute.Diffuse, texture));
		}
	}

	public void generateWorld() {
		for (Chunk chunk : chunkArray) {
			for (int x = 0; x < CHUNK_SIZE; x++) {
				for (int z = 0; z < CHUNK_SIZE; z++) {
					biome.processColumn(chunk, x, z, seed, CHUNK_HEIGHT);
				}
			}
		}
		Debugger.dumpLayerInfo();
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

	public void set(int x, int y, int z, Block voxel) {
		int chunkX = x / CHUNK_SIZE;
		if (chunkX < 0 || chunkX >= chunksX) return;
		int chunkY = y / CHUNK_HEIGHT;
		if (chunkY < 0 || chunkY >= chunksY) return;
		int chunkZ = z / CHUNK_SIZE;
		if (chunkZ < 0 || chunkZ >= chunksZ) return;
		chunkArray[chunkX + chunkZ * chunksX + chunkY * chunksX * chunksZ].set(x % CHUNK_SIZE, y % CHUNK_HEIGHT, z % CHUNK_SIZE, voxel);
	}

	public Block get(BlockPos pos) {
		return get(pos.x(), pos.y(), pos.z());
	}

	public Block get(int x, int y, int z) {
		int chunkX = x / CHUNK_SIZE;
		if (chunkX < 0 || chunkX >= chunksX) return Blocks.AIR;
		int chunkY = y / CHUNK_HEIGHT;
		if (chunkY < 0 || chunkY >= chunksY) return Blocks.AIR;
		int chunkZ = z / CHUNK_SIZE;
		if (chunkZ < 0 || chunkZ >= chunksZ) return Blocks.AIR;
		
		return chunkArray[chunkX + chunkZ * chunksX + chunkY * chunksX * chunksZ].get(x % CHUNK_SIZE, y % CHUNK_HEIGHT, z % CHUNK_SIZE);
	}

	public float getHighest(float x, float z) {
		int ix = (int)x;
		int iz = (int)z;
		if (ix < 0 || ix >= voxelsX) return 0;
		if (iz < 0 || iz >= voxelsZ) return 0;
		// FIXME optimize
		for (int y = voxelsY - 1; y > 0; y--) {
			if (get(ix, y, iz) != Blocks.AIR) return y + 1;
		}
		return 0;
	}

	public void setColumn(float x, float y, float z, Block voxel) {
		int ix = (int)x;
		int iy = (int)y;
		int iz = (int)z;
		if (ix < 0 || ix >= voxelsX) return;
		if (iy < 0 || iy >= voxelsY) return;
		if (iz < 0 || iz >= voxelsZ) return;
		// FIXME optimize
		for (; iy > 0; iy--) {
			set(ix, iy, iz, voxel);
		}
	}

	public void setCube(float x, float y, float z, float width, float height, float depth, Block voxel) {
		int ix = (int)x;
		int iy = (int)y;
		int iz = (int)z;
		int iwidth = (int)width;
		int iheight = (int)height;
		int idepth = (int)depth;
		int startX = Math.max(ix, 0);
		int endX = Math.min(voxelsX, ix + iwidth);
		int startY = Math.max(iy, 0);
		int endY = Math.min(voxelsY, iy + iheight);
		int startZ = Math.max(iz, 0);
		int endZ = Math.min(voxelsZ, iz + idepth);
		// FIXME optimize
		for (iy = startY; iy < endY; iy++) {
			for (iz = startZ; iz < endZ; iz++) {
				for (ix = startX; ix < endX; ix++) {
					set(ix, iy, iz, voxel);
				}
			}
		}
	}

	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		renderedChunks = 0;
		for (int i = 0; i < chunkArray.length; i++) {
			Chunk chunk = chunkArray[i];
			Mesh mesh = meshArray[i];
			if (dirty[i]) {
				int numVerts = chunk.calculateVertices(vertices);
				numVertices[i] = numVerts / 4 * 6;
				mesh.setVertices(vertices, 0, numVerts * Chunk.VERTEX_SIZE);
				dirty[i] = false;
			}
			if (numVertices[i] == 0) continue;
			Renderable renderable = pool.obtain();
			renderable.material = materialArray[i];
			renderable.meshPart.mesh = mesh;
			renderable.meshPart.offset = 0;
			renderable.meshPart.size = numVertices[i];
			renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;
			renderables.add(renderable);
			renderedChunks++;
		}
	}

	public void regen() {
		int i = 0;
		for (int y = 0; y < chunksY; y++) {
			for (int z = 0; z < chunksZ; z++) {
				for (int x = 0; x < chunksX; x++) {
					Chunk chunk = new Chunk(CHUNK_SIZE, CHUNK_HEIGHT);
					chunk.offset.set(x * CHUNK_SIZE, y * CHUNK_HEIGHT, z * CHUNK_SIZE);
					chunkArray[i++] = chunk;
				}
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

	public BoundingBox collide(BoundingBox box, EnumFacing facing) {
		int xMin = MathUtils.floor(box.min.x);
		int xMax = MathUtils.floor(box.max.x);
		int yMin = MathUtils.floor(box.min.y);
		int yMax = MathUtils.floor(box.max.y);
		int zMin = MathUtils.floor(box.min.z);
		int zMax = MathUtils.floor(box.max.z);

		AxisDirection axisDirection = facing.getAxisDirection();
		int step = axisDirection == AxisDirection.NEGATIVE ? -1 : 1;

		switch (facing.getAxis()) {
			case X -> {
				var min = axisDirection == AxisDirection.NEGATIVE ? xMax : xMin;
				var max = axisDirection == AxisDirection.NEGATIVE ? xMin : xMax;

				for (int x = min; (axisDirection == AxisDirection.NEGATIVE) == (x > max); x+=step) {
					for (int y = yMin; y <= yMax; y++) {
						for (int z = zMin; z <= zMax; z++) {
							System.out.println("x = " + x + ", y = " + y + ", z = " + z);
							Block block = this.get(x, y, z);
							if (block != null && block.isSolid()) {
								BoundingBox blockBox = block.getBoundingBox(x, y, z);
								if (blockBox != null && blockBox.intersects(box)) {
									return blockBox;
								}
							}
						}
					}
				}
			}
			case Y -> {
				var min = axisDirection == AxisDirection.NEGATIVE ? yMax : yMin;
				var max = axisDirection == AxisDirection.NEGATIVE ? yMin : yMax;

				for (int y = min; (axisDirection == AxisDirection.NEGATIVE) == (y > max); y+=step) {
					for (int x = xMin; x <= xMax; x++) {
						for (int z = zMin; z <= zMax; z++) {
							System.out.println("x = " + x + ", y = " + y + ", z = " + z);
							Block block = this.get(x, y, z);
							if (block != null && block.isSolid()) {
								BoundingBox blockBox = block.getBoundingBox(x, y, z);
								if (blockBox != null && blockBox.intersects(box)) {
									return blockBox;
								}
							}
						}
					}
				}
			}
			case Z -> {
				var min = axisDirection == AxisDirection.NEGATIVE ? zMax : zMin;
				var max = axisDirection == AxisDirection.NEGATIVE ? zMin : zMax;

				for (int z = min; (axisDirection == AxisDirection.NEGATIVE) == (z > max); z+=step) {
					for (int x = xMin; x <= xMax; x++) {
						for (int y = yMin; y <= yMax; y++) {
							System.out.println("x = " + x + ", y = " + y + ", z = " + z);
							Block block = this.get(x, y, z);
							if (block != null && block.isSolid()) {
								BoundingBox blockBox = block.getBoundingBox(x, y, z);
								if (blockBox != null && blockBox.intersects(box)) {
									return blockBox;
								}
							}
						}
					}
				}
			}
		}

		return null;
	}
}
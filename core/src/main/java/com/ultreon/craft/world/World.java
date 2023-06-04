package com.ultreon.craft.world;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.google.common.collect.Range;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.util.Utils;
import com.ultreon.craft.util.WorldRayCaster;
import com.ultreon.craft.world.gen.BiomeGenerator;
import com.ultreon.craft.world.gen.GenerationStage;
import com.ultreon.craft.world.gen.TerrainGenerator;
import com.ultreon.craft.world.gen.WorldGenInfo;
import com.ultreon.craft.world.gen.feature.TreeFeature;
import com.ultreon.craft.world.gen.layer.*;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.crash.v0.CrashCategory;
import com.ultreon.libs.crash.v0.CrashLog;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
	public static final int WORLD_DEPTH = 0;
	private static final Logger LOGGER = LoggerFactory.getLogger("World");
	private final short[] indices;
	private float[] vertices;

	private final BiomeGenerator biome = BiomeGenerator.builder()
			.noise(NoiseSettingsInit.DEFAULT)
			.domainWarping(new DomainWarping(NoiseSettingsInit.DOMAIN_X, NoiseSettingsInit.DOMAIN_Y))
			.layer(new WaterTerrainLayer(64))
			.layer(new AirTerrainLayer())
			.layer(new SurfaceTerrainLayer())
			.layer(new StoneTerrainLayer())
			.layer(new UndergroundTerrainLayer())
			.feature(new TreeFeature(GenerationStage.TREES, Range.open(3, 5)))
			.extraLayer(new StonePatchTerrainLayer(NoiseSettingsInit.STONE_PATCH, new DomainWarping(NoiseSettingsInit.DOMAIN_X, NoiseSettingsInit.DOMAIN_Y)))
			.build();
	private final long seed = 512;
	private int renderedChunks;

	private final Map<ChunkPos, RawChunk> chunks = new ConcurrentHashMap<>();
	private TerrainGenerator terrainGen;
	private final Int2ReferenceMap<Entity> entities = new Int2ReferenceArrayMap<>();
	private int playTime;
	private int curId;
	private final UltreonCraft game = UltreonCraft.get();
	private int totalChunks;

	public World(int chunksX, int chunksZ) {
		this.vertices = new float[Chunk.VERTEX_SIZE * 6 * CHUNK_SIZE * WORLD_HEIGHT * CHUNK_SIZE];

		int len = World.CHUNK_SIZE * World.CHUNK_HEIGHT * World.CHUNK_SIZE * 6 * 6 / 3;

		indices = new short[len];
		short j = 0;
		for (int i = 0; i < len; i += 6, j += 4) {
			indices[i] = j;
			indices[i + 1] = (short) (j + 1);
			indices[i + 2] = (short) (j + 2);
			indices[i + 3] = (short) (j + 2);
			indices[i + 4] = (short) (j + 3);
			indices[i + 5] = j;
		}
	}

	private WorldGenInfo getWorldGenInfo(Vector3 pos) {
		List<ChunkPos> needed = getChunksAround(this, pos);
		List<ChunkPos> chunkPositionsToCreate = getChunksToLoad(needed, pos);
		List<ChunkPos> chunkPositionsToRemove = getChunksToUnload(needed);

		WorldGenInfo data = new WorldGenInfo();
		data.toCreate = chunkPositionsToCreate;
		data.toRemove = chunkPositionsToRemove;
		data.toUpdate = new ArrayList<>();
		return data;

	}

	static List<ChunkPos> getChunksAround(World world, Vector3 pos) {
		int startX = (int) (pos.x - (world.getRenderDistance()) * World.CHUNK_SIZE);
		int startZ = (int) (pos.z - (world.getRenderDistance()) * World.CHUNK_SIZE);
		int endX = (int) (pos.x + (world.getRenderDistance()) * World.CHUNK_SIZE);
		int endZ = (int) (pos.z + (world.getRenderDistance()) * World.CHUNK_SIZE);

		List<ChunkPos> toCreate = new ArrayList<>();
		for (int x = startX; x <= endX; x += World.CHUNK_SIZE) {
			for (int z = startZ; z <= endZ; z += World.CHUNK_SIZE) {
				ChunkPos chunkPos = Utils.chunkPosFromBlockCoords(new Vector3(x, 0, z));
				toCreate.add(chunkPos);
				if (x >= pos.x - World.CHUNK_SIZE
						&& x <= pos.x + World.CHUNK_SIZE
						&& z >= pos.z - World.CHUNK_SIZE
						&& z <= pos.z + World.CHUNK_SIZE) {
					for (int y = -World.CHUNK_HEIGHT; y >= pos.y - World.CHUNK_HEIGHT * 2; y -= World.CHUNK_HEIGHT) {
						chunkPos = Utils.chunkPosFromBlockCoords(new Vector3(x, y, z));
						toCreate.add(chunkPos);
					}
				}
			}
		}

		return toCreate;
	}

	private int getRenderDistance() {
		return game.settings.getRenderDistance();
	}

	private List<ChunkPos> getChunksToUnload(List<ChunkPos> needed) {
		List<ChunkPos> toRemove = new ArrayList<>();
		for (var pos : chunks.keySet().stream().filter(pos -> !needed.contains(pos) && !getChunk(pos).modifiedByPlayer).toList()) {
			if (this.getChunk(pos) != null) {
				toRemove.add(pos);
			}
		}

		return toRemove;
	}

	 private List<ChunkPos> getChunksToLoad(List<ChunkPos> needed, Vector3 pos) {
		return needed.stream()
				.filter(chunkPos -> this.getChunk(chunkPos) == null)
				.sorted((o1, o2) -> Float.compare(o1.toVector3().dst(pos), o2.toVector3().dst(pos)))
				.toList();
	}

	public void updateChunksForPlayerAsync(Player player) {
		CompletableFuture.runAsync(() -> updateChunksForPlayer(player.getPosition()));
	}

	private void updateChunksForPlayer(Vector3 player) {
		WorldGenInfo worldGenInfo = getWorldGenInfo(player);
		worldGenInfo.toRemove.forEach(this::unloadChunk);
		worldGenInfo.toCreate.forEach(this::generateChunk);
		worldGenInfo.toCreate = null;
		worldGenInfo.toRemove = null;
	}

	private void unloadChunk(ChunkPos chunkPos) {
		unloadChunk(getChunk(chunkPos));
	}

	private void unloadChunk(RawChunk rawChunk) {
		synchronized (rawChunk.lock) {
			if (rawChunk instanceof Chunk chunk1) {
				chunk1.ready = false;
			}
			chunks.remove(rawChunk.pos);
			rawChunk.dispose();
		}
	}

	public HitResult rayCast(Ray ray) {
		return WorldRayCaster.rayCast(new HitResult(ray), this);
	}

	protected void generateChunk(ChunkPos pos) {
		generateChunk(pos.x, pos.z);
	}

	protected void generateChunk(int x, int z) {
		ChunkPos pos = new ChunkPos(x, z);
		RawChunk oldChunk = getChunk(x, z);
		BuilderChunk chunk;

		if (oldChunk == null) chunk = new BuilderChunk(this, CHUNK_SIZE, CHUNK_HEIGHT, pos);
		else if (oldChunk instanceof BuilderChunk oldBuilderChunk) chunk = oldBuilderChunk;
		else return;

		for (int dx = x - 1; dx <= x + 1; dx++) {
			for (int dz = z - 1; dz <= z + 1; dz++) {
				if (dx == x && dz == z) continue;

				RawChunk at = getChunk(dx, dz);
				if (at == null) {
					putChunk(pos, new BuilderChunk(this, CHUNK_SIZE, CHUNK_HEIGHT, new ChunkPos(dx, dz)));
				}
			}
		}

		for (int bx = 0; bx < CHUNK_SIZE; bx++) {
			for (int by = 0; by < CHUNK_SIZE; by++) {
				biome.processColumn(this, chunk, bx, by, seed);
			}
		}

		Chunk build = chunk.build();
		putChunk(pos, build);
	}

	private void putChunk(ChunkPos chunkPos, RawChunk chunk) {
		RawChunk oldChunk = chunks.get(chunkPos);
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

	@SuppressWarnings("BlockingMethodInNonBlockingContext")
	public CompletableFuture<ConcurrentMap<Vector3, RawChunk>> generateWorldChunkData(List<ChunkPos> toCreate) {
		ConcurrentMap<Vector3, RawChunk> map = new ConcurrentHashMap<>();
		return CompletableFuture.supplyAsync(() -> {
			for (var pos : toCreate) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				RawChunk chunk = new BuilderChunk(this, CHUNK_SIZE, CHUNK_HEIGHT, pos);
				RawChunk newChunk = terrainGen.generateChunkData(this, chunk, seed);
			}
			return map;
		});
	}

	public void set(GridPoint3 blockPos, Block block) {
		set(blockPos.x, blockPos.y, blockPos.z, block);
	}

	public void set(int x, int y, int z, Block block) {
		RawChunk chunk = getChunkAt(x, y, z);
		GridPoint3 cp = toChunkCoords(x, y, z);
		chunk.set(cp.x, cp.y, cp.z, block);
	}

	public Block get(GridPoint3 pos) {
		return get(pos.x, pos.y, pos.z);
	}

	@NotNull
	public Block getRaw(int x, int y, int z) {
		RawChunk chunkAt = getChunkAt(x, y, z);
		if (chunkAt == null) {
			return Blocks.AIR;
		}

		GridPoint3 cp = toChunkCoords(x, y, z);
		return chunkAt.get(cp.x, cp.y, cp.z);
	}

	@NotNull
	public Block get(int x, int y, int z) {
		RawChunk chunkAt = getChunkAt(x, y, z);
		if (!(chunkAt instanceof Chunk chunk)) return Blocks.AIR;
		if (!chunk.ready) {
			return Blocks.AIR;
		}

		GridPoint3 cp = toChunkCoords(x, y, z);
		return chunkAt.get(cp.x, cp.y, cp.z);
	}

	private GridPoint3 toChunkCoords(GridPoint3 worldCoords) {
		return toChunkCoords(worldCoords.x, worldCoords.y, worldCoords.z);
	}

	private GridPoint3 toChunkCoords(int x, int y, int z) {
		int cx = x % CHUNK_SIZE;
		int cy = y % CHUNK_HEIGHT;
		int cz = z % CHUNK_SIZE;

		if (cx < 0) cx += CHUNK_SIZE;
		if (cz < 0) cz += CHUNK_SIZE;

		return new GridPoint3(cx, cy, cz);
	}

	public RawChunk getChunk(ChunkPos chunkPos) {
		return chunks.get(chunkPos);
	}

	public RawChunk getChunk(int x, int z) {
		return getChunk(new ChunkPos(x, z));
	}

	public RawChunk getChunkAt(int x, int y, int z) {
		return getChunkAt(new GridPoint3(x, y, z));
	}

	public RawChunk getChunkAt(GridPoint3 pos) {
		int chunkX = Math.floorDiv(pos.x, CHUNK_SIZE);
		int chunkZ = Math.floorDiv(pos.z, CHUNK_SIZE);

		ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
		return getChunk(chunkPos);
	}

	public int getHighest(int x, int z) {
		RawChunk chunkAt = getChunkAt(x, 0, z);
		if (chunkAt == null) return 0;

		// FIXME: Optimize by using a heightmap.
		for (int y = CHUNK_HEIGHT - 1; y > 0; y--) {
			if (get(x, y, z) != Blocks.AIR) return y + 1;
		}
		return 0;
	}

	public void setColumn(int x, int z, Block block) {
		setColumn(x, z, CHUNK_HEIGHT, block);
	}

	public void setColumn(int x, int z, int maxY, Block block) {
		if (getChunkAt(x, maxY, z) == null) return;

		// FIXME optimize
		for (; maxY > 0; maxY--) {
			set(x, maxY, z, block);
		}
	}

	// TODO: Port to new chunk system.
	@Deprecated
	public void set(int x, int y, int z, int width, int height, int depth, Block block) {
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
//					set(ix, iy, iz, block);
//				}
//			}
//		}
	}

	@Override
	public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
		renderedChunks = 0;
		totalChunks = chunks.size();
		List<RawChunk> toSort = new ArrayList<>(chunks.values());
		toSort.sort((o1, o2) -> {
			Vector3 mid1 = new Vector3(o1.offset.x + (float) CHUNK_SIZE / 2, o1.offset.y + (float) CHUNK_HEIGHT / 2, o1.offset.z + (float) CHUNK_SIZE / 2);
			Vector3 mid2 = new Vector3(o2.offset.x + (float) CHUNK_SIZE / 2, o2.offset.y + (float) CHUNK_HEIGHT / 2, o2.offset.z + (float) CHUNK_SIZE / 2);
			return Float.compare(mid2.dst(this.game.player.getPosition()), mid1.dst(this.game.player.getPosition()));
		});
		for (RawChunk chunk : toSort) {
			synchronized (chunk.lock) {
				if (!chunk.ready) {
					continue;
				}

				Mesh opaqueMesh = chunk.mesh;
				if (chunk.dirty) {
					int numVertices = chunk.calculateVertices(this.vertices);
					chunk.numVertices = numVertices / 4 * 6;
					opaqueMesh.setVertices(this.vertices, 0, numVertices * RawChunk.VERTEX_SIZE);
					chunk.dirty = false;
				}
				if (chunk.numVertices == 0) {
					continue;
				}
				Renderable piece = pool.obtain();

				piece.material = chunk.material;
				piece.meshPart.mesh = opaqueMesh;
				piece.meshPart.offset = 0;
				piece.meshPart.size = chunk.numVertices;
				piece.meshPart.primitiveType = GL20.GL_TRIANGLES;

				renderables.add(piece);
				renderedChunks = renderedChunks + 1;
			}
		}
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
		for (RawChunk chunk : chunks.values()) {
			chunk.dispose();
		}
		vertices = null;
	}

	public void updateChunksForPlayer(float spawnX, float spawnZ) {
		this.updateChunksForPlayer(new Vector3(spawnX, 0, spawnZ));
	}

	public int getRenderedChunks() {
		return renderedChunks;
	}

	public int getTotalChunks() {
		return totalChunks;
	}

	public void fillCrashInfo(CrashLog crashLog) {
		CrashCategory cat = new CrashCategory("World Details");
		cat.add("Total chunks", this.totalChunks); // Too many chunks?
		cat.add("Rendered chunks", this.renderedChunks); // ChunkRW render overflow?
		cat.add("Seed", this.seed); // For weird world generation glitches

		crashLog.addCategory(cat);
	}

	public long getSeed() {
		return seed;
	}
}
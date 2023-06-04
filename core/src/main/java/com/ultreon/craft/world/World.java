package com.ultreon.craft.world;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.Entities;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.util.Utils;
import com.ultreon.craft.util.WorldRayCaster;
import com.ultreon.craft.world.gen.BiomeGenerator;
import com.ultreon.craft.world.gen.TerrainGenerator;
import com.ultreon.craft.world.gen.WorldGenInfo;
import com.ultreon.craft.world.gen.layer.*;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.crash.v0.CrashCategory;
import com.ultreon.libs.crash.v0.CrashLog;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
	private final SavedWorld savedWorld;
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
			.extraLayer(new StonePatchTerrainLayer(NoiseSettingsInit.STONE_PATCH, new DomainWarping(NoiseSettingsInit.DOMAIN_X, NoiseSettingsInit.DOMAIN_Y)))
			.build();
	private final long seed = 512;
	private int renderedChunks;

	private final Map<ChunkPos, Chunk> chunks = new ConcurrentHashMap<>();
	private TerrainGenerator terrainGen;
	private final Int2ReferenceMap<Entity> entities = new Int2ReferenceArrayMap<>();
	private int playTime;
	private int curId;
	private final UltreonCraft game = UltreonCraft.get();
	private int totalChunks;

	public World(SavedWorld savedWorld, int chunksX, int chunksZ) {
		this.savedWorld = savedWorld;

		this.vertices = new float[Chunk.VERTEX_SIZE * 6 * CHUNK_SIZE * WORLD_HEIGHT * CHUNK_SIZE];

		int len = World.CHUNK_SIZE * World.CHUNK_HEIGHT * World.CHUNK_SIZE * 6 * 6 / 3;

		this.indices = new short[len];
		short j = 0;
		for (int i = 0; i < len; i += 6, j += 4) {
			this.indices[i] = j;
			this.indices[i + 1] = (short) (j + 1);
			this.indices[i + 2] = (short) (j + 2);
			this.indices[i + 3] = (short) (j + 2);
			this.indices[i + 4] = (short) (j + 3);
			this.indices[i + 5] = j;
		}
	}

	@ApiStatus.Internal
	public void load() throws IOException {
		this.savedWorld.createDir("data/");
		this.savedWorld.createDir("chunks/");

		if (this.savedWorld.exists("data/entities.ubo")) {
			ListType<MapType> entitiesData = this.savedWorld.read("data/entities.ubo");
			for (MapType entityData : entitiesData) {
				Entity entity = Entity.loadFrom(this, entityData);
				this.entities.put(entity.getId(), entity);
			}
		}

		if (this.savedWorld.exists("data/player.ubo")) {
			MapType playerData = this.savedWorld.read("data/player.ubo");
			Player player = Entities.PLAYER.create(this);
			player.loadWithPos(playerData);
			UltreonCraft.get().player = player;
		}
	}

	@ApiStatus.Internal
	public void save() throws IOException {
		LOGGER.info("Saving world: " + this.savedWorld.getDirectory().getName());

		ListType<MapType> entitiesData = new ListType<>();
		for (Entity entity : this.entities.values()) {
			if (entity instanceof Player) continue;

			MapType entityData = entity.save(new MapType());
			entitiesData.add(entityData);
		}
		this.savedWorld.write(entitiesData, "data/entities.ubo");

		MapType playerData = new MapType();
		this.savedWorld.write(playerData, "data/player.ubo");

		for (Chunk chunk : this.chunks.values()) {
			try {
				this.savedWorld.writeChunk(chunk.pos.x, chunk.pos.z, chunk.save());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

		LOGGER.info("Saved world: " + this.savedWorld.getDirectory().getName());
	}

	@ApiStatus.Internal
	public CompletableFuture<Boolean> saveAsync() throws IOException {
		return CompletableFuture.supplyAsync(() -> {
			try {
				this.save();
				return true;
			} catch (Exception e) {
				LOGGER.error("Failed to save world", e);
				return false;
			}
		});
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
		worldGenInfo.toCreate.forEach(this::loadChunk);
		worldGenInfo.toCreate = null;
		worldGenInfo.toRemove = null;
	}

	private void unloadChunk(ChunkPos chunkPos) {
		this.unloadChunk(this.getChunk(chunkPos));
	}

	@CanIgnoreReturnValue
	private CompletableFuture<Void> unloadChunk(Chunk chunk) {
		synchronized (chunk.lock) {
			chunk.ready = false;
			this.chunks.remove(chunk.pos);
			return CompletableFuture.runAsync(() -> {
				try {
					this.savedWorld.writeChunk(chunk.pos.x, chunk.pos.z, chunk.save());
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				chunk.dispose();
			});
		}
	}

	public HitResult rayCast(Ray ray) {
		return WorldRayCaster.rayCast(new HitResult(ray), this);
	}

	protected void loadChunk(ChunkPos pos) {
		this.loadChunk(pos.x, pos.z);
	}

	protected void loadChunk(int x, int z) {
		if (this.savedWorld.chunkExists(x, z)) {
			Chunk chunk;
			ChunkPos pos = new ChunkPos(x, z);
			try {
				chunk = Chunk.load(this, pos, this.savedWorld.readChunk(x, z));
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			this.game.runLater(() -> {
				chunk.ready = true;
				this.putChunk(pos, chunk);
			});
		} else {
			this.generateChunk(x, z);
		}
	}

	protected void generateChunk(ChunkPos pos) {
		this.generateChunk(pos.x, pos.z);
	}

	protected void generateChunk(int x, int z) {
		ChunkPos chunkPos = new ChunkPos(x, z);
		Chunk chunk = new Chunk(this, CHUNK_SIZE, CHUNK_HEIGHT, chunkPos);
		chunk.offset.set(x * CHUNK_SIZE, WORLD_DEPTH, z * CHUNK_SIZE);
		chunk.dirty = false;
		chunk.numVertices = 0;
		chunk.material = new Material(new TextureAttribute(TextureAttribute.Diffuse, this.game.blocksTextureAtlas.getTexture()));
		chunk.material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
		chunk.material.set(new DepthTestAttribute(GL20.GL_DEPTH_FUNC));

		for (int bx = 0; bx < CHUNK_SIZE; bx++) {
			for (int by = 0; by < CHUNK_SIZE; by++) {
				this.biome.processColumn(chunk, bx, by, this.seed, CHUNK_HEIGHT);
			}
		}

		this.game.runLater(() -> {
			chunk.mesh = new Mesh(false, false, CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE * 6 * 4,
					CHUNK_SIZE * CHUNK_HEIGHT * CHUNK_SIZE * 36 / 3, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)));
			chunk.mesh.setIndices(this.indices);
			chunk.ready = true;
			chunk.dirty = true;
			this.putChunk(chunkPos, chunk);
		});
	}

	private void putChunk(ChunkPos chunkPos, Chunk chunk) {
		Chunk oldChunk = this.chunks.get(chunkPos);
		if (oldChunk != null) {
			oldChunk.dispose();
		}
		this.chunks.put(chunkPos, chunk);
	}

	public void tick() {
		playTime++;

		for (var entity : entities.values()) {
			entity.tick();
		}
	}

	@SuppressWarnings("BlockingMethodInNonBlockingContext")
	public CompletableFuture<ConcurrentMap<Vector3, Chunk>> generateWorldChunkData(List<ChunkPos> toCreate) {
		ConcurrentMap<Vector3, Chunk> map = new ConcurrentHashMap<>();
		return CompletableFuture.supplyAsync(() -> {
			for (var pos : toCreate) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				Chunk chunk = new Chunk(this, CHUNK_SIZE, CHUNK_HEIGHT, pos);
				Chunk newChunk = terrainGen.generateChunkData(chunk, seed);
			}
			return map;
		});
	}

	public void set(GridPoint3 blockPos, Block block) {
		set(blockPos.x, blockPos.y, blockPos.z, block);
	}

	public void set(int x, int y, int z, Block block) {
		Chunk chunk = getChunkAt(x, y, z);
		GridPoint3 cp = toChunkCoords(x, y, z);
		chunk.set(cp.x, cp.y, cp.z, block);
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

	public Chunk getChunk(ChunkPos chunkPos) {
		return chunks.get(chunkPos);
	}

	public Chunk getChunkAt(int x, int y, int z) {
		return getChunkAt(new GridPoint3(x, y, z));
	}

	public Chunk getChunkAt(GridPoint3 pos) {
		int chunkX = Math.floorDiv(pos.x, CHUNK_SIZE);
		int chunkZ = Math.floorDiv(pos.z, CHUNK_SIZE);

		ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
		return getChunk(chunkPos);
	}

	public int getHighest(int x, int z) {
		Chunk chunkAt = getChunkAt(x, 0, z);
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
		List<Chunk> toSort = new ArrayList<>(chunks.values());
		toSort.sort((o1, o2) -> {
			Vector3 mid1 = new Vector3(o1.offset.x + (float) CHUNK_SIZE / 2, o1.offset.y + (float) CHUNK_HEIGHT / 2, o1.offset.z + (float) CHUNK_SIZE / 2);
			Vector3 mid2 = new Vector3(o2.offset.x + (float) CHUNK_SIZE / 2, o2.offset.y + (float) CHUNK_HEIGHT / 2, o2.offset.z + (float) CHUNK_SIZE / 2);
			return Float.compare(mid2.dst(this.game.player.getPosition()), mid1.dst(this.game.player.getPosition()));
		});
		for (Chunk chunk : toSort) {
			synchronized (chunk.lock) {
				if (!chunk.ready) {
					continue;
				}

				Mesh opaqueMesh = chunk.mesh;
				if (chunk.dirty) {
					int numVertices = chunk.calculateVertices(this.vertices);
					chunk.numVertices = numVertices / 4 * 6;
					opaqueMesh.setVertices(this.vertices, 0, numVertices * Chunk.VERTEX_SIZE);
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
		for (Chunk chunk : chunks.values()) {
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
		cat.add("Rendered chunks", this.renderedChunks); // Chunk render overflow?
		cat.add("Seed", this.seed); // For weird world generation glitches

		crashLog.addCategory(cat);
	}

	public SavedWorld getSavedWorld() {
		return savedWorld;
	}
}
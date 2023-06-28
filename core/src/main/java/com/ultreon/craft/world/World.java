package com.ultreon.craft.world;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
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
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.Constants;
import com.ultreon.craft.Task;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.Entities;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.input.GameInput;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.util.Utils;
import com.ultreon.craft.util.WorldRayCaster;
import com.ultreon.craft.util.exceptions.ValueMismatchException;
import com.ultreon.craft.world.gen.BiomeGenerator;
import com.ultreon.craft.world.gen.TerrainGenerator;
import com.ultreon.craft.world.gen.WorldGenInfo;
import com.ultreon.craft.world.gen.layer.*;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.crash.v0.CrashCategory;
import com.ultreon.libs.crash.v0.CrashLog;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.ultreon.craft.UltreonCraft.LOGGER;
import static com.ultreon.craft.world.WorldRegion.REGION_SIZE;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class World implements RenderableProvider, Disposable {
	public static final int CHUNK_SIZE = 16;
	public static final int CHUNK_HEIGHT = 256;
	public static final int WORLD_HEIGHT = 256;
	public static final int WORLD_DEPTH = 0;
	public static final Marker MARKER = MarkerFactory.getMarker("World");
	private static final Biome DEFAULT_BIOME = Biome.builder()
			.noise(NoiseSettingsInit.DEFAULT)
			.domainWarping((seed) -> new DomainWarping(NoiseSettingsInit.DOMAIN_X.create(seed), NoiseSettingsInit.DOMAIN_Y.create(seed)))
			.layer(new WaterTerrainLayer(64))
			.layer(new AirTerrainLayer())
			.layer(new SurfaceTerrainLayer())
			.layer(new StoneTerrainLayer())
			.layer(new UndergroundTerrainLayer())
//			.extraLayer(new StonePatchTerrainLayer(NoiseSettingsInit.STONE_PATCH))
			.build();

	private final BiomeGenerator generator;
	private final SavedWorld savedWorld;
	private final short[] indices;
	private final GridPoint3 spawnPoint = new GridPoint3();
	private float[] vertices;

	private final long seed = 512;
	private int renderedChunks;

	private final Map<RegionPos, WorldRegion> regions = new ConcurrentHashMap<>();
	private TerrainGenerator terrainGen;
	private final Int2ReferenceMap<Entity> entities = new Int2ReferenceArrayMap<>();
	private final Map<ChunkPos, CompletableFuture<Chunk>> loadingChunks = new ConcurrentHashMap<>();
	private int playTime;
	private int curId;
	private final UltreonCraft game = UltreonCraft.get();
	private int totalChunks;

	static {
		// TODO: Use biome registry
		DEFAULT_BIOME.buildLayers();
	}

	private CompletableFuture<Boolean> saveFuture;
	private ScheduledFuture<?> saveSchedule;
	private int chunksToLoad;
	private int chunksLoaded;
	private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();

	public World(SavedWorld savedWorld, int chunksX, int chunksZ) {
		this.savedWorld = savedWorld;

		this.vertices = new float[Chunk.VERTEX_SIZE * 6 * CHUNK_SIZE * WORLD_HEIGHT * CHUNK_SIZE];

		int len = World.CHUNK_SIZE * World.CHUNK_SIZE * World.CHUNK_SIZE * 6 * 6 / 3;

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

		this.generator = DEFAULT_BIOME.create(this, this.seed);
	}

	@ApiStatus.Internal
	public void load() throws IOException {
		this.savedWorld.createDir("data/");
		this.savedWorld.createDir("regions/");

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

		this.saveSchedule = this.game.schedule(new Task(new Identifier("auto_save")) {
			@Override
			public void run() {
				try {
					World.this.save(true);
				} catch (Exception e) {
					LOGGER.error(MARKER, "Failed to save world", e);
				}
				World.this.saveSchedule = World.this.game.schedule(this, Constants.AUTO_SAVE_DELAY, Constants.AUTO_SAVE_DELAY_UNIT);
			}
		}, Constants.INITIAL_AUTO_SAVE_DELAY, Constants.AUTO_SAVE_DELAY_UNIT);
	}

	@ApiStatus.Internal
	public void save(boolean silent) throws IOException {
		if (!silent) LOGGER.info(MARKER, "Saving world: " + this.savedWorld.getDirectory().name());

		ListType<MapType> entitiesData = new ListType<>();
		for (Entity entity : this.entities.values()) {
			if (entity instanceof Player) continue;

			MapType entityData = entity.save(new MapType());
			entitiesData.add(entityData);
		}
		this.savedWorld.write(entitiesData, "data/entities.ubo");

		Player player = UltreonCraft.get().player;
		MapType playerData = player == null ? new MapType() : player.save(new MapType());
		this.savedWorld.write(playerData, "data/player.ubo");

		for (Map.Entry<RegionPos, WorldRegion> entry : this.regions.entrySet()) {
			try {
				WorldRegion region = entry.getValue();
				RegionPos pos = entry.getKey();
				region.save();
				if (region.isEmpty()) {
					region.dispose(true);
					this.regions.remove(pos);
				}
			} catch (IOException e) {
				LOGGER.error(MARKER, "Failed to save region:", e);
				return;
			}
		}

		if (!silent) LOGGER.info(MARKER, "Saved world: " + this.savedWorld.getDirectory().name());
	}

	@ApiStatus.Internal
	public CompletableFuture<Boolean> saveAsync(boolean silent) {
		if (this.saveFuture != null && !this.saveSchedule.isDone()) {
			return this.saveFuture;
		}
		if (this.saveSchedule != null) {
			this.saveSchedule.cancel(false);
		}
		return this.saveFuture = CompletableFuture.supplyAsync(() -> {
			try {
				this.save(silent);
				return true;
			} catch (Exception e) {
				LOGGER.error(MARKER, "Failed to save world", e);
				return false;
			}
		}, this.saveExecutor);
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
		return game.settings.renderDistance.get();
	}

	private List<ChunkPos> getChunksToUnload(List<ChunkPos> needed) {
		List<ChunkPos> toRemove = new ArrayList<>();
		for (ChunkPos pos : this.getChunks().stream().map(chunk -> chunk.pos).filter(pos -> {
			Chunk chunk = this.getChunk(pos);
//			System.out.println("pos = " + pos);
//			System.out.println("needed = " + needed);
//			System.out.println("needed.stream().filter(chunkPos -> chunkPos == pos) = " + needed.stream().filter(pos::equals).collect(Collectors.toList()));
			return chunk != null && !needed.contains(pos);
		}).collect(Collectors.toList())) {
			if (this.getChunk(pos) != null) {
				toRemove.add(pos);
			}
		}

		return toRemove;
	}

	private boolean isAlwaysLoaded(ChunkPos pos) {
		return this.isSpawnChunk(pos);
	}

	private List<ChunkPos> getChunksToLoad(List<ChunkPos> needed, Vector3 pos) {
		return needed.stream()
				.filter(chunkPos -> this.getChunk(chunkPos) == null)
				.sorted((o1, o2) -> Float.compare(o1.getChunkOrigin().dst(pos), o2.getChunkOrigin().dst(pos)))
				.collect(Collectors.toList());
	}

	public CompletableFuture<Void> updateChunksForPlayerAsync(Player player) {
		return CompletableFuture.runAsync(() -> this.updateChunksForPlayer(player.getPosition()));
	}

	private CompletableFuture<Void> updateChunksForPlayerAsync(Vector3 position) {
		return CompletableFuture.runAsync(() -> this.updateChunksForPlayer(position));
	}

	public void updateChunksForPlayer(Player player) {
		this.updateChunksForPlayerAsync(player.getPosition());
	}

	public void updateChunksForPlayer(float x, float z) {
		this.updateChunksForPlayer(new Vector3(x, WORLD_DEPTH, z));
	}

	public void updateChunksForPlayer(Vector3 player) {
		WorldGenInfo worldGenInfo = this.getWorldGenInfo(player);
		List<ChunkPos> toCreate = worldGenInfo.toCreate;
		this.chunksToLoad = toCreate.size();
		this.chunksLoaded = 0;
//		System.out.println("worldGenInfo.toRemove = " + worldGenInfo.toRemove);
		for (ChunkPos chunkPos : worldGenInfo.toRemove) {
			this.unloadChunk(chunkPos);
		}
		for (ChunkPos chunkPos : toCreate) {
			this.loadChunk(chunkPos);
			this.chunksLoaded++;
		}
		worldGenInfo.toCreate = null;
		worldGenInfo.toRemove = null;
	}

	private CompletableFuture<Boolean> unloadChunkAsync(ChunkPos chunkPos) {
		return this.unloadChunkAsync(Objects.requireNonNull(this.getChunk(chunkPos), "Chunk not loaded: " + chunkPos));
	}

	@CanIgnoreReturnValue
	private CompletableFuture<Boolean> unloadChunkAsync(@NotNull Chunk chunk) {
		synchronized (chunk.lock) {
            LOGGER.debug(MARKER, "UNLOAD:: chunk.pos = " + chunk.pos, new RuntimeException());
            return CompletableFuture.supplyAsync(() -> {
				WorldRegion region = this.getRegionFor(chunk.pos);
				if (region == null) {
					return false;
				}
				synchronized (region.lock) {
					region.unloadChunk(this.toLocalChunkPos(chunk.pos.x(), chunk.pos.z()), true, false);
					return true;
				}
			});
		}
	}

	private boolean unloadChunk(@NotNull ChunkPos chunkPos) {
		Chunk chunk = this.getChunk(chunkPos);
		if (chunk == null) return true;
		return this.unloadChunk(chunk);
	}

	@CanIgnoreReturnValue
	private boolean unloadChunk(@NotNull Chunk chunk) {
		synchronized (chunk.lock) {
			WorldRegion region = this.getRegionFor(chunk.pos);
			if (region == null) {
				return false;
			}
			synchronized (region.lock) {
				region.unloadChunk(this.toLocalChunkPos(chunk.pos.x(), chunk.pos.z()), true, false);
				return true;
			}
		}
	}

	@Nullable
	private WorldRegion getRegionFor(ChunkPos chunkPos) {
		RegionPos regionPos = new RegionPos(Math.floorDiv(chunkPos.x(), REGION_SIZE), Math.floorDiv(chunkPos.z(), REGION_SIZE));
		return this.getRegion(regionPos);
	}

	private WorldRegion getRegion(RegionPos regionPos) {
		WorldRegion region = this.regions.get(regionPos);
		if (region != null && !region.getPosition().equals(regionPos)) {
			throw new ValueMismatchException("Position of region received (" + region.getPosition() + ") doesn't match the requested position (" + regionPos + ")");
		}
		return region;
	}

	private WorldRegion getOrOpenRegionFor(ChunkPos chunkPos, boolean loadAsync) {
		RegionPos regionPos = new RegionPos(Math.floorDiv(chunkPos.x(), REGION_SIZE), Math.floorDiv(chunkPos.z(), REGION_SIZE));
		ChunkPos localChunkPos = this.toLocalChunkPos(chunkPos.x(), chunkPos.z());
		return this.getOrOpenRegion(regionPos, loadAsync);
	}

	private CompletableFuture<WorldRegion> getOrOpenRegionForAsync(ChunkPos chunkPos) {
		RegionPos regionPos = new RegionPos(Math.floorDiv(chunkPos.x(), REGION_SIZE), Math.floorDiv(chunkPos.z(), REGION_SIZE));
		ChunkPos localChunkPos = this.toLocalChunkPos(chunkPos.x(), chunkPos.z());
		return this.getOrOpenRegionAsync(regionPos);
	}

	private WorldRegion getOrOpenRegion(RegionPos regionPos, boolean loadAsync) {
		WorldRegion oldRegion = this.regions.get(regionPos);
		if (oldRegion != null) {
			return oldRegion;
		}
		WorldRegion region = new WorldRegion(this, regionPos, loadAsync);
		if (region.isCorrupt()) {
			LOGGER.warn(MARKER, "Corrupted region: " + regionPos);
		}
		return this.regions.computeIfAbsent(regionPos, pos -> region);
	}

	private CompletableFuture<WorldRegion> getOrOpenRegionAsync(RegionPos regionPos) {
		WorldRegion oldRegion = this.regions.get(regionPos);
		if (oldRegion != null) {
			return CompletableFuture.completedFuture(oldRegion);
		}
		return CompletableFuture.supplyAsync(() -> {
			WorldRegion region = new WorldRegion(this, regionPos, false);
			if (region.isCorrupt()) {
				LOGGER.warn(MARKER, "Corrupted region: " + regionPos);
			}
			return this.regions.computeIfAbsent(regionPos, pos -> region);
		});
	}

	public HitResult rayCast(Ray ray) {
		return WorldRayCaster.rayCast(new HitResult(ray), this);
	}

	protected CompletableFuture<Chunk> loadChunkAsync(ChunkPos pos) {
		return this.loadChunkAsync(pos.x(), pos.z());
	}

	public CompletableFuture<Chunk> loadChunkAsync(int x, int z) {
		return this.loadChunkAsync(x, z, false);
	}

	public CompletableFuture<Chunk> loadChunkAsync(int x, int z, boolean overwrite) {
		ChunkPos pos = new ChunkPos(x, z);
		CompletableFuture<Chunk> loadingChunk = this.loadingChunks.get(pos);
		if (loadingChunk != null) {
			if (loadingChunk.isDone()) this.loadingChunks.remove(pos);
			return loadingChunk;
		}

		ChunkPos localPos = this.toLocalChunkPos(x, z);
		int regionX = Math.floorDiv(x, REGION_SIZE);
		int regionZ = Math.floorDiv(z, REGION_SIZE);

		CompletableFuture<Chunk> future = this.getOrOpenRegionAsync(new RegionPos(regionX, regionZ)).thenApplyAsync(region -> {
			try {
				Chunk oldChunk = region.getChunk(localPos);

				if (oldChunk != null && !overwrite) {
					return oldChunk;
				}

				Chunk loadedChunk = region.loadChunk(localPos);
				Chunk chunk;
				if (loadedChunk == null) {
					chunk = this.generateChunk(x, z);
				} else {
					chunk = loadedChunk;
				}
				if (chunk == null) {
					LOGGER.warn(MARKER, "Tried to load chunk at {} but it still wasn't loaded:", pos);
					return oldChunk;
				}

				this.renderChunk(x, z, chunk);
				return chunk;
			} catch (RuntimeException e) {
				LOGGER.error(MARKER, "Failed to load chunk {}:", pos, e);
				throw e;
			}
		});
		this.loadingChunks.put(pos, future);
		return future;
	}

	protected Chunk loadChunk(ChunkPos pos) {
		return this.loadChunk(pos.x(), pos.z());
	}

	public Chunk loadChunk(int x, int z) {
		return this.loadChunk(x, z, false);
	}

	public synchronized Chunk loadChunk(int x, int z, boolean overwrite) {
		ChunkPos pos = new ChunkPos(x, z);
		CompletableFuture<Chunk> loadingChunk = this.loadingChunks.get(pos);
		if (loadingChunk != null) {
			if (loadingChunk.isDone()) this.loadingChunks.remove(pos);
			return loadingChunk.join();
		}
		loadingChunk = new CompletableFuture<>();

		ChunkPos localPos = this.toLocalChunkPos(x, z);
		int regionX = Math.floorDiv(x, REGION_SIZE);
		int regionZ = Math.floorDiv(z, REGION_SIZE);

		WorldRegion region = this.getOrOpenRegion(new RegionPos(regionX, regionZ), false);
		try {
			Chunk oldChunk = region.getChunk(localPos);

			if (oldChunk != null && !overwrite) {
				loadingChunk.complete(oldChunk);
				return oldChunk;
			}

			Chunk loadedChunk = region.loadChunk(localPos);
			Chunk chunk;
			if (loadedChunk == null) {
				chunk = this.generateChunk(x, z);
			} else {
				chunk = loadedChunk;
			}
			if (chunk == null) {
				LOGGER.warn(MARKER, "Tried to load chunk at {} but it still wasn't loaded:", pos);
				loadingChunk.complete(oldChunk);
				return oldChunk;
			}

			this.renderChunk(x, z, chunk);
			loadingChunk.complete(chunk);
			return chunk;
		} catch (RuntimeException e) {
			LOGGER.error(MARKER, "Failed to load chunk {}:", pos, e);
			throw e;
		}
	}

	protected CompletableFuture<Chunk> generateChunkAsync(ChunkPos pos) {
		return this.generateChunkAsync(pos.x(), pos.z());
	}

	protected CompletableFuture<Chunk> generateChunkAsync(int x, int z) {
		return CompletableFuture.supplyAsync(() -> this.generateChunk(x, z));
	}

	protected @Nullable Chunk generateChunk(ChunkPos pos) {
		return this.generateChunk(pos.x(), pos.z());
	}

	@Nullable
	protected Chunk generateChunk(int x, int z) {
		ChunkPos pos = new ChunkPos(x, z);
		Chunk chunk = new Chunk(this, CHUNK_SIZE, CHUNK_HEIGHT, pos);

		WorldRegion region = this.getRegionFor(pos);

		if (region == null) return null;

		try {
			if (!this.putChunk(region, pos, chunk)) {
				LOGGER.warn(MARKER, "Tried to overwrite chunk {}", chunk.pos);
				chunk.dispose();
				return null;
			}

			for (int bx = 0; bx < CHUNK_SIZE; bx++) {
				for (int by = 0; by < CHUNK_SIZE; by++) {
					this.generator.processColumn(chunk, bx, by, CHUNK_HEIGHT);
				}
			}

			region.initialized = true;

			return chunk;
		} catch (Exception e) {
			LOGGER.error(MARKER, "Failed to generate chunk {}:", pos, e);
			return null;
		}
	}

	private void renderChunk(int x, int z, Chunk chunk) {
		chunk.dirty = false;
		for (Section section : chunk.getSections()) {
			section.numVertices = 0;
			section.material = new Material(new TextureAttribute(TextureAttribute.Diffuse, this.game.blocksTextureAtlas.getTexture()));
			section.material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
			section.material.set(new DepthTestAttribute(GL20.GL_DEPTH_FUNC));

			this.game.runLater(new Task(new Identifier("post_section_render"), () -> {
				section.ready = true;
				section.dirty = true;
			}));
		}

		this.game.runLater(new Task(new Identifier("post_chunk_render"), () -> {
			chunk.ready = true;
			chunk.dirty = true;
		}));
	}

	@CanIgnoreReturnValue
	private boolean putChunk(@NotNull WorldRegion region, @NotNull ChunkPos chunkPos, @NotNull Chunk chunk) {
		return this.putChunk(region, chunkPos, chunk, false);
	}

	@SuppressWarnings("SameParameterValue")
	private boolean putChunk(@NotNull WorldRegion region, @NotNull ChunkPos chunkPos, @NotNull Chunk chunk, boolean overwrite) {
		return region.putChunk(this.toLocalChunkPos(chunk.pos.x(), chunk.pos.z()), chunk, overwrite);
	}

	public void tick() {
		this.playTime++;

		for (Entity entity : this.entities.values()) {
			entity.tick();
		}
	}

	public CompletableFuture<ConcurrentMap<Vector3, Chunk>> generateWorldChunkData(List<ChunkPos> toCreate) {
		ConcurrentMap<Vector3, Chunk> map = new ConcurrentHashMap<>();
		return CompletableFuture.supplyAsync(() -> {
			for (ChunkPos pos : toCreate) {
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
		GridPoint3 cp = toLocalBlockPos(x, y, z);
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

		synchronized (chunkAt.lock) {
			if (!chunkAt.ready) return Blocks.AIR;

			GridPoint3 cp = this.toLocalBlockPos(x, y, z);
			return chunkAt.get(cp.x, cp.y, cp.z);
		}
	}

	private GridPoint3 toLocalBlockPos(GridPoint3 worldCoords) {
		return this.toLocalBlockPos(worldCoords.x, worldCoords.y, worldCoords.z);
	}

	private GridPoint3 toLocalBlockPos(int x, int y, int z) {
		int cx = x % CHUNK_SIZE;
		int cy = y % CHUNK_HEIGHT;
		int cz = z % CHUNK_SIZE;

		if (cx < 0) cx += CHUNK_SIZE;
		if (cz < 0) cz += CHUNK_SIZE;

		return new GridPoint3(cx, cy, cz);
	}

	private ChunkPos toLocalChunkPos(int x, int z) {
		int cx = x % REGION_SIZE;
		int cz = z % REGION_SIZE;

		if (cx < 0) cx += REGION_SIZE;
		if (cz < 0) cz += REGION_SIZE;

		return new ChunkPos(cx, cz);
	}

	@Nullable
	public Chunk getChunk(ChunkPos chunkPos) {
		WorldRegion region = this.getRegionFor(chunkPos);
		if (region == null) {
			return null;
		}
		Chunk chunk = region.getChunk(this.toLocalChunkPos(chunkPos.x(), chunkPos.z()));
		if (chunk != null && !chunk.pos.equals(chunkPos)) {
			throw new ValueMismatchException("Position of chunk received (" + chunk.pos + ") doesn't match the requested position (" + chunkPos + ")");
		}
		return chunk;
	}

	public Chunk getChunkAt(int x, int y, int z) {
		return this.getChunkAt(new GridPoint3(x, y, z));
	}

	public Chunk getChunkAt(GridPoint3 pos) {
		int chunkX = Math.floorDiv(pos.x, CHUNK_SIZE);
		int chunkZ = Math.floorDiv(pos.z, CHUNK_SIZE);

		if (this.isOutOfWorldBounds(pos)) return null;

		ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
		return this.getChunk(chunkPos);
	}

	private boolean isOutOfWorldBounds(GridPoint3 pos) {
		return pos.y < WORLD_DEPTH || pos.y > WORLD_HEIGHT;
	}

	public int getHighest(int x, int z) {
		Chunk chunkAt = this.getChunkAt(x, 0, z);
		if (chunkAt == null) return 0;

		// FIXME: Optimize by using a heightmap.
		for (int y = CHUNK_HEIGHT - 1; y > 0; y--) {
			if (this.get(x, y, z) != Blocks.AIR) return y + 1;
		}
		return 0;
	}

	public void setColumn(int x, int z, Block block) {
		this.setColumn(x, z, CHUNK_HEIGHT, block);
	}

	public void setColumn(int x, int z, int maxY, Block block) {
		if (this.getChunkAt(x, maxY, z) == null) return;

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
		this.renderedChunks = 0;
		List<Chunk> chunks = this.getChunks();
		this.totalChunks = chunks.size();
		List<Chunk> toSort = new ArrayList<>(chunks);
		Player player = this.game.player;
		if (player == null) return;
		toSort.sort((o1, o2) -> {
			Vector3 mid1 = new Vector3(o1.getOffset().x + (float) CHUNK_SIZE / 2, o1.getOffset().y + (float) CHUNK_HEIGHT / 2, o1.getOffset().z + (float) CHUNK_SIZE / 2);
			Vector3 mid2 = new Vector3(o2.getOffset().x + (float) CHUNK_SIZE / 2, o2.getOffset().y + (float) CHUNK_HEIGHT / 2, o2.getOffset().z + (float) CHUNK_SIZE / 2);
			return Float.compare(mid2.dst(player.getPosition()), mid1.dst(player.getPosition()));
		});
		for (Chunk chunk1 : toSort) {
			if (!chunk1.ready) continue;

			for (Section section : chunk1.getSections()) {
				synchronized (section.lock) {
					if (!section.ready) continue;

					Mesh mesh = section.mesh;
					if (section.dirty || section.mesh == null) {
						if (section.mesh != null) section.mesh.dispose();
						FloatList vertices = new FloatArrayList();
						int numVertices = section.buildVertices(vertices);
						mesh = section.mesh = new Mesh(false, false, numVertices,
								this.indices.length * 6, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)));
						section.mesh.setIndices(this.indices);
						section.numVertices = numVertices / 4 * 6;
						section.mesh.setVertices(vertices.toFloatArray());
						vertices.clear();
						section.dirty = false;
					}

					if (section.numVertices == 0) continue;

					Renderable piece = pool.obtain();

					piece.material = section.material;
					piece.meshPart.mesh = mesh;
					piece.meshPart.offset = 0;
					piece.meshPart.size = section.numVertices;
					piece.meshPart.primitiveType = GL20.GL_TRIANGLES;

					renderables.add(piece);
					this.renderedChunks = this.renderedChunks + 1;
				}
			}
		}
	}

	private List<Chunk> getChunks() {
		List<Chunk> chunks = new ArrayList<>();
		for (WorldRegion region : this.regions.values()) {
			Collection<Chunk> regionChunks = region.getChunks();
			chunks.addAll(regionChunks);
		}
		return chunks;
	}

	public int getPlayTime() {
		return this.playTime;
	}

	public <T extends Entity> T spawn(T entity) {
		this.setEntityId(entity);
		this.entities.put(entity.getId(), entity);
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

	@Override
	public void dispose() {
		GameInput.cancelVibration();

		this.saveSchedule.cancel(true);
		this.saveExecutor.shutdownNow();

		try {
			this.save(true);
		} catch (IOException e) {
			LOGGER.warn(MARKER, "Saving failed:", e);
		}

		for (WorldRegion chunk : this.regions.values()) {
			chunk.dispose(true);
		}
		this.generator.dispose();
		this.vertices = null;
	}

	public CompletableFuture<Void> updateChunksForPlayerAsync(float spawnX, float spawnZ) {
		return this.updateChunksForPlayerAsync(new Vector3(spawnX, 0, spawnZ));
	}

	public int getRenderedChunks() {
		return this.renderedChunks;
	}

	public int getTotalChunks() {
		return this.totalChunks;
	}

	public void fillCrashInfo(CrashLog crashLog) {
		CrashCategory cat = new CrashCategory("World Details");
		cat.add("Total chunks", this.totalChunks); // Too many chunks?
		cat.add("Rendered chunks", this.renderedChunks); // Chunk render overflow?
		cat.add("Seed", this.seed); // For weird world generation glitches

		crashLog.addCategory(cat);
	}

	public boolean intersectEntities(BoundingBox boundingBox) {
		for (var entity : entities.values())
			if (entity.getBoundingBox().intersects(boundingBox)) return true;

		return false;
	}

	public SavedWorld getSavedWorld() {
		return savedWorld;
	}

	public WorldRegion getRegionAt(GridPoint3 blockPos) {
		RegionPos regionPos = new RegionPos(Math.floorDiv(Math.floorDiv(blockPos.x, CHUNK_SIZE), REGION_SIZE), Math.floorDiv(Math.floorDiv(blockPos.z, CHUNK_SIZE), REGION_SIZE));
		return this.getRegion(regionPos);
	}

	public long getSeed() {
		return this.seed;
	}

	public void setSpawnPoint(int spawnX, int spawnZ) {
		this.spawnPoint.set(spawnX, this.getHighest(spawnX, spawnZ), spawnZ);
	}

	public boolean isSpawnChunk(ChunkPos pos) {
		int x = pos.x();
		int z = pos.z();

		return this.spawnPoint.x - 1 <= x && this.spawnPoint.x + 1 >= x &&
				this.spawnPoint.z - 1 <= z && this.spawnPoint.z + 1 >= z;
	}

	public GridPoint3 getSpawnPoint() {
		this.spawnPoint.y = this.getHighest(this.spawnPoint.x, this.spawnPoint.z);
		return this.spawnPoint;
	}

	public int getChunksToLoad() {
		return this.chunksToLoad;
	}

	public int getChunksLoaded() {
		return this.chunksLoaded;
	}
}
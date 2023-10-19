package com.ultreon.craft.world;

import static com.ultreon.craft.UltreonCraft.LOGGER;

import com.google.common.base.Preconditions;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.util.BoundingBox;
import com.ultreon.craft.util.Ray;
import com.badlogic.gdx.utils.Disposable;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.Constants;
import com.ultreon.craft.Task;
import com.ultreon.craft.UltreonCraft;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.Entities;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.events.BlockEvents;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.input.GameInput;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.util.Utils;
import com.ultreon.craft.util.WorldRayCaster;
import com.ultreon.craft.util.exceptions.ValueMismatchException;
import com.ultreon.craft.world.gen.BiomeGenerator;
import com.ultreon.craft.world.gen.TerrainGenerator;
import com.ultreon.craft.world.gen.WorldGenInfo;
import com.ultreon.craft.world.gen.layer.AirTerrainLayer;
import com.ultreon.craft.world.gen.layer.StoneTerrainLayer;
import com.ultreon.craft.world.gen.layer.SurfaceTerrainLayer;
import com.ultreon.craft.world.gen.layer.UndergroundTerrainLayer;
import com.ultreon.craft.world.gen.layer.WaterTerrainLayer;
import com.ultreon.craft.world.gen.noise.DomainWarping;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;
import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.crash.v0.CrashCategory;
import com.ultreon.libs.crash.v0.CrashLog;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@ParametersAreNonnullByDefault
public class World implements Disposable {
	public static final int CHUNK_SIZE = 16;
	public static final int CHUNK_HEIGHT = 256;
	public static final int WORLD_HEIGHT = 256;
	public static final int WORLD_DEPTH = 0;
	public static final Marker MARKER = MarkerFactory.getMarker("World");
	private static final int REGION_SIZE = 32;
	private static long chunkUnloads;
	private static long chunkLoads;
	private static long chunkSaves;

	private final Map<ChunkPos, Chunk> chunks = new ConcurrentHashMap<>();
	private static final Biome DEFAULT_BIOME = Biome.builder()
			.noise(NoiseSettingsInit.DEFAULT)
			.domainWarping(seed -> new DomainWarping(UltreonCraft.get().deferDispose(NoiseSettingsInit.DOMAIN_X.create(seed)), UltreonCraft.get().deferDispose(NoiseSettingsInit.DOMAIN_Y.create(seed))))
			.layer(new WaterTerrainLayer(64))
			.layer(new AirTerrainLayer())
			.layer(new SurfaceTerrainLayer())
			.layer(new StoneTerrainLayer())
			.layer(new UndergroundTerrainLayer())
//			.extraLayer(new StonePatchTerrainLayer(NoiseSettingsInit.STONE_PATCH))
			.build();

	private final BiomeGenerator generator;
	private final SavedWorld savedWorld;
	private final Vec3i spawnPoint = new Vec3i();
	private final long seed = 512;
	private int renderedChunks;

	@Nullable
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
	@Nullable
	private CompletableFuture<Boolean> saveFuture;
	@Nullable
	private ScheduledFuture<?> saveSchedule;
	private int chunksToLoad;
	private int chunksLoaded;
	private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();
	private final List<ChunkPos> alwaysLoaded = new ArrayList<>();
	private final ExecutorService executor = Executors.newCachedThreadPool();

	public World(SavedWorld savedWorld, int chunksX, int chunksZ) {
		this.savedWorld = savedWorld;

		this.generator = DEFAULT_BIOME.create(this, this.seed);
	}

	public static long getChunkUnloads() {
		return chunkUnloads;
	}

	public static long getChunkLoads() {
		return World.chunkLoads;
	}

	public static long getChunkSaves() {
		return World.chunkSaves;
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

		if (this.savedWorld.exists("player.ubo")) {
			MapType playerData = this.savedWorld.read("player.ubo");
			Player player = Entities.PLAYER.create(this);
			player.loadWithPos(playerData);
			UltreonCraft.get().player = player;
		}

		if (this.savedWorld.exists("data/player.ubo")) {
			MapType playerData = this.savedWorld.read("data/player.ubo");
			Player player = Entities.PLAYER.create(this);
			player.loadWithPos(playerData);
			UltreonCraft.get().player = player;
		}

		WorldEvents.LOAD_WORLD.factory().onLoadWorld(this, this.savedWorld);

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

		this.game.notifications.unavailable("World Saving");

		// TODO add saving back
//		for (Map.Entry<RegionPos, WorldRegion> entry : this.regions.entrySet()) {
//			try {
//				WorldRegion region = entry.getValue();
//				RegionPos pos = entry.getKey();
//				region.save();
//				if (region.isEmpty()) {
//					region.disposeNow();
//					this.regions.remove(pos);
//				}
//			} catch (IOException e) {
//				LOGGER.error(MARKER, "Failed to save region:", e);
//				return;
//			}
//		}

		WorldEvents.SAVE_WORLD.factory().onSaveWorld(this, this.savedWorld);

		if (!silent) LOGGER.info(MARKER, "Saved world: " + this.savedWorld.getDirectory().name());
	}

	@ApiStatus.Internal
	public CompletableFuture<Boolean> saveAsync(boolean silent) {
		ScheduledFuture<?> saveSchedule = this.saveSchedule;
		if (saveSchedule != null && !saveSchedule.isDone()) {
			return this.saveFuture != null ? this.saveFuture : CompletableFuture.completedFuture(true);
		}
		if (saveSchedule != null) {
			saveSchedule.cancel(false);
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

	private WorldGenInfo getWorldGenInfo(Vec3d pos) {
		List<ChunkPos> needed = getChunksAround(this, pos);
		List<ChunkPos> chunkPositionsToCreate = this.getChunksToLoad(needed, pos);
		List<ChunkPos> chunkPositionsToRemove = this.getChunksToUnload(needed);

		WorldGenInfo data = new WorldGenInfo();
		data.toCreate = chunkPositionsToCreate;
		data.toRemove = chunkPositionsToRemove;
		data.toUpdate = new ArrayList<>();
		return data;

	}

	static List<ChunkPos> getChunksAround(World world, Vec3d pos) {
		int startX = (int) (pos.x - world.getRenderDistance() * World.CHUNK_SIZE);
		int startZ = (int) (pos.z - world.getRenderDistance() * World.CHUNK_SIZE);
		int endX = (int) (pos.x + world.getRenderDistance() * World.CHUNK_SIZE);
		int endZ = (int) (pos.z + world.getRenderDistance() * World.CHUNK_SIZE);

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
		return this.game.settings.renderDistance.get();
	}

	private List<ChunkPos> getChunksToUnload(List<ChunkPos> needed) {
		List<ChunkPos> toRemove = new ArrayList<>();
		for (ChunkPos pos : this.getLoadedChunks().stream().map(chunk -> chunk.pos).filter(pos -> {
			Chunk chunk = this.getChunk(pos);
			return chunk != null && !needed.contains(pos);
		}).collect(Collectors.toList())) {
			if (this.getChunk(pos) != null) {
				toRemove.add(pos);
			}
		}

		return toRemove;
	}

	private boolean shouldStayLoaded(ChunkPos pos) {
		return false;
//		return this.isSpawnChunk(pos) || this.isAlwaysLoaded(pos);
	}

	public boolean isAlwaysLoaded(ChunkPos pos) {
		return this.alwaysLoaded.contains(pos);
	}

	private List<ChunkPos> getChunksToLoad(List<ChunkPos> needed, Vec3d pos) {
		return needed.stream()
				.filter(chunkPos -> this.getChunk(chunkPos) == null)
				.sorted(Comparator.comparingDouble(o -> o.getChunkOrigin().dst(pos)))
				.collect(Collectors.toList());
	}

	public CompletableFuture<Void> updateChunksForPlayerAsync(Player player) {
		return CompletableFuture.runAsync(() -> this.updateChunksForPlayer(player.getPosition()));
	}

	private CompletableFuture<Void> updateChunksForPlayerAsync(Vec3d position) {
		return CompletableFuture.runAsync(() -> this.updateChunksForPlayer(position), this.executor);
	}

	public void updateChunksForPlayer(Player player) {
		this.updateChunksForPlayerAsync(player.getPosition());
	}

	public void updateChunksForPlayer(float x, float z) {
		this.updateChunksForPlayer(new Vec3d(x, WORLD_DEPTH, z));
	}

	public void updateChunksForPlayer(Vec3d player) {
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
		worldGenInfo.toCreate.clear();
		worldGenInfo.toRemove.clear();
	}

	private CompletableFuture<Boolean> unloadChunkAsync(ChunkPos chunkPos) {
		return this.unloadChunkAsync(Objects.requireNonNull(this.getChunk(chunkPos), "Chunk not loaded: " + chunkPos));
	}

	@CanIgnoreReturnValue
	private CompletableFuture<Boolean> unloadChunkAsync(@NotNull Chunk chunk) {
		synchronized (chunk.lock) {
			LOGGER.debug(MARKER, "UNLOAD:: chunk.pos = " + chunk.pos, new RuntimeException());
			return CompletableFuture.supplyAsync(() -> this.unloadChunk(chunk, chunk.pos));
		}
	}

	private boolean unloadChunk(@NotNull ChunkPos chunkPos) {
		Chunk chunk = this.getChunk(chunkPos);
		if (chunk == null) return true;
		return this.unloadChunk(chunk, chunkPos);
	}

	@CanIgnoreReturnValue
	private boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkPos pos) {
		synchronized (chunk.lock) {
			if (!chunk.pos.equals(pos)) {
				throw new IllegalArgumentException("Chunk position (" + chunk.pos + ") and provided position (" + pos + ") don't match.");
			}
			synchronized (chunk.lock) {
				if (!this.unloadChunk(pos, true, false))
					LOGGER.warn(MARKER, "Failed to unload chunk at " + pos);

				WorldEvents.CHUNK_UNLOADED.factory().onChunkUnloaded(this, chunk.pos, chunk);
				return true;
			}
		}
	}

	@CanIgnoreReturnValue
	public boolean unloadChunk(ChunkPos localChunkPos, boolean save) {
		return this.unloadChunk(localChunkPos, save, false);
	}

	@CanIgnoreReturnValue
	public boolean unloadChunk(ChunkPos localChunkPos, boolean save, boolean force) {
		boolean flag = true;
		if (save && !(flag = this.saveChunk(localChunkPos)) && !force) return false;

		Chunk chunk = this.chunks.remove(localChunkPos);
		if (chunk == null) {
			LOGGER.warn("Tried to unload non-existing chunk: " + localChunkPos);
			return false;
		}

		World.chunkUnloads++;
		chunk.dispose();
		return flag;
	}

	@CanIgnoreReturnValue
	public boolean saveChunk(ChunkPos chunkPos) {
		Chunk chunk = this.chunks.get(chunkPos);

		if (chunk == null) return false;
		MapType data = chunk.save();

		World.chunkSaves++;

		// TODO add saving back
//		this.data.put(chunkPos.toString(), data);
		return true;
	}

	@Nullable
	@Deprecated
	private WorldRegion getRegionFor(ChunkPos chunkPos) {
//		RegionPos regionPos = new RegionPos(Math.floorDiv(chunkPos.x(), REGION_SIZE), Math.floorDiv(chunkPos.z(), REGION_SIZE));
//		return this.getRegion(regionPos);
		return null;
	}

	@Nullable
	@Deprecated
	private WorldRegion getRegion(RegionPos regionPos) {
//		WorldRegion region = this.regions.get(regionPos);
//		if (region != null && !region.getPosition().equals(regionPos)) {
//			throw new ValueMismatchException("Position of region received (" + region.getPosition() + ") doesn't match the requested position (" + regionPos + ")");
//		}
		return null;
	}

	@Deprecated
	private WorldRegion getOrOpenRegionFor(ChunkPos chunkPos, boolean loadAsync) {
//		RegionPos regionPos = new RegionPos(Math.floorDiv(chunkPos.x(), REGION_SIZE), Math.floorDiv(chunkPos.z(), REGION_SIZE));
//        return this.getOrOpenRegion(regionPos, loadAsync);
		return null;
	}

	@Deprecated
	private CompletableFuture<WorldRegion> getOrOpenRegionForAsync(ChunkPos chunkPos) {
//		RegionPos regionPos = new RegionPos(Math.floorDiv(chunkPos.x(), REGION_SIZE), Math.floorDiv(chunkPos.z(), REGION_SIZE));
//        return this.getOrOpenRegionAsync(regionPos);
		return null;
	}

	@Deprecated
	private WorldRegion getOrOpenRegion(RegionPos regionPos, boolean loadAsync) {
//		WorldRegion oldRegion = this.regions.get(regionPos);
//		if (oldRegion != null) {
//			return oldRegion;
//		}
//		WorldRegion region = new WorldRegion(this, regionPos, loadAsync);
//		if (region.isCorrupt()) {
//			LOGGER.warn(MARKER, "Corrupted region: " + regionPos);
//		}
//		return this.regions.computeIfAbsent(regionPos, pos -> region);
		return null;
	}

	private CompletableFuture<WorldRegion> getOrOpenRegionAsync(RegionPos regionPos) {
//		WorldRegion oldRegion = this.regions.get(regionPos);
//		if (oldRegion != null) {
//			return CompletableFuture.completedFuture(oldRegion);
//		}
		return CompletableFuture.supplyAsync(() -> {
//			WorldRegion region = new WorldRegion(this, regionPos, false);
//			if (region.isCorrupt()) {
//				LOGGER.warn(MARKER, "Corrupted region: " + regionPos);
//			}
//			return this.regions.computeIfAbsent(regionPos, pos -> region);
			return null;
		}, this.executor);
	}

	public HitResult rayCast(Ray ray) {
		return WorldRayCaster.rayCast(new HitResult(ray), this);
	}

	public HitResult rayCast(Ray ray, float distance) {
		HitResult hitResult = new HitResult(ray);
		hitResult.distanceMax = distance;
		return WorldRayCaster.rayCast(hitResult, this);
	}

	protected CompletableFuture<Chunk> loadChunkAsync(ChunkPos pos) {
		return this.loadChunkAsync(pos.x(), pos.z());
	}

	public CompletableFuture<Chunk> loadChunkAsync(int x, int z) {
		return this.loadChunkAsync(x, z, false);
	}

	public CompletableFuture<Chunk> loadChunkAsync(int x, int z, boolean overwrite) {
		return CompletableFuture.supplyAsync(() -> this.loadChunk(x, z, overwrite));
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
//		int regionX = Math.floorDiv(x, REGION_SIZE);
//		int regionZ = Math.floorDiv(z, REGION_SIZE);

		try {
			Chunk oldChunk = this.getChunk(localPos);

			if (oldChunk != null && !overwrite) {
				loadingChunk.complete(oldChunk);
				return oldChunk;
			}

			Chunk loadedChunk = this.loadChunkFromDisk(localPos);
			Chunk chunk;
			if (loadedChunk == null) {
				chunk = this.generateChunk(x, z);
			} else {
				chunk = loadedChunk;
			}
			if (chunk == null) {
				LOGGER.warn(MARKER, "Tried to load chunk at " + pos + " but it still wasn't loaded:");
				if (oldChunk != null) loadingChunk.complete(oldChunk);
				else throw new IllegalStateException("Chunk loading failed: chunk wasn't loaded while requested to load");
				return oldChunk;
			}

			this.renderChunk(x, z, chunk);
			loadingChunk.complete(chunk);
			WorldEvents.CHUNK_LOADED.factory().onChunkLoaded(this, pos, chunk);
            World.chunkLoads++;

			return chunk;
		} catch (RuntimeException e) {
			LOGGER.error(MARKER, "Failed to load chunk " + pos + ":", e);
			throw e;
		}
	}

	private Chunk loadChunkFromDisk(ChunkPos chunkPos) {
		// TODO implement loading chunks from disk again.
//		ChunkPos worldChunkPos = new ChunkPos(chunkPos.x(), chunkPos.z());
//		MapType mapType = this.get(chunkPos);
//		if (mapType == null) return null;
//
//		Chunk chunk = Chunk.load(worldChunkPos, mapType);
//
//		synchronized (this.lock) {
//			this.chunks.put(chunkPos, chunk);
//		}
//
//		return chunk;
		return null;
	}

	protected CompletableFuture<@Nullable Chunk> generateChunkAsync(ChunkPos pos) {
		return this.generateChunkAsync(pos.x(), pos.z());
	}

	protected CompletableFuture<@Nullable Chunk> generateChunkAsync(int x, int z) {
		return CompletableFuture.supplyAsync(() -> this.generateChunk(x, z), executor);
	}

	protected @Nullable Chunk generateChunk(ChunkPos pos) {
		return this.generateChunk(pos.x(), pos.z());
	}

	@Nullable
	protected Chunk generateChunk(int x, int z) {
		ChunkPos pos = new ChunkPos(x, z);
		Chunk chunk = new Chunk(CHUNK_SIZE, CHUNK_HEIGHT, pos);

		try {
			for (int bx = 0; bx < CHUNK_SIZE; bx++) {
				for (int by = 0; by < CHUNK_SIZE; by++) {
					this.generator.processColumn(chunk, bx, by, CHUNK_HEIGHT);
				}
			}

			if (!this.putChunk(pos, chunk)) {
				LOGGER.warn(MARKER, "Tried to overwrite chunk " + chunk.pos);
				chunk.dispose();
				return null;
			}

			WorldEvents.CHUNK_GENERATED.factory().onChunkGenerated(this, pos, chunk);

			return chunk;
		} catch (Exception e) {
			LOGGER.error(MARKER, "Failed to generate chunk " + pos + ":", e);
			return null;
		}
	}

	private void renderChunk(int x, int z, Chunk chunk) {
		for (Section section : chunk.getSections()) {
			this.game.submit(new Task(new Identifier("post_section_render"), () -> {
				section.dirty = true;
				section.ready = true;
			}));
		}

		this.game.submit(new Task(new Identifier("post_chunk_render"), () -> {
			chunk.dirty = true;
			chunk.ready = true;
		}));
	}

	@CanIgnoreReturnValue
	private boolean putChunk(@NotNull ChunkPos chunkPos, @NotNull Chunk chunk) {
		return this.putChunk(chunkPos, chunk, false);
	}

	public boolean putChunk(@NotNull ChunkPos chunkPos, @NotNull Chunk chunk, boolean overwrite) {
		if (!overwrite)
			return this.chunks.putIfAbsent(chunkPos, chunk) == null;

		Chunk oldChunk = this.chunks.put(chunkPos, chunk);
		if (oldChunk != null)
			oldChunk.dispose();

		return true;
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

				Chunk chunk = new Chunk(CHUNK_SIZE, CHUNK_HEIGHT, pos);
				assert this.terrainGen != null;
				Chunk newChunk = this.terrainGen.generateChunkData(chunk, this.seed);
			}
			return map;
		});
	}

	public void set(Vec3i blockPos, Block block) {
		this.set(blockPos.x, blockPos.y, blockPos.z, block);
	}

	public void set(int x, int y, int z, Block block) {
		BlockEvents.SET_BLOCK.factory().onSetBlock(this, new Vec3i(x, y, z), block);

		Chunk chunk = this.getChunkAt(x, y, z);
		if (chunk == null) return;

		Vec3i cp = this.toLocalBlockPos(x, y, z);
		chunk.set(cp.x, cp.y, cp.z, block);
	}

	public Block get(Vec3i pos) {
		return this.get(pos.x, pos.y, pos.z);
	}

	public Block get(int x, int y, int z) {
		Chunk chunkAt = this.getChunkAt(x, y, z);
		if (chunkAt == null) {
			return Blocks.AIR;
		}

		synchronized (chunkAt.lock) {
			if (!chunkAt.ready) return Blocks.AIR;

			Vec3i cp = this.toLocalBlockPos(x, y, z);
			return chunkAt.getFast(cp.x, cp.y, cp.z);
		}
	}

	private Vec3i toLocalBlockPos(Vec3i worldCoords) {
		return this.toLocalBlockPos(worldCoords.x, worldCoords.y, worldCoords.z);
	}

	private Vec3i toLocalBlockPos(int x, int y, int z) {
		int cx = x % CHUNK_SIZE;
		int cy = y % CHUNK_HEIGHT;
		int cz = z % CHUNK_SIZE;

		if (cx < 0) cx += CHUNK_SIZE;
		if (cz < 0) cz += CHUNK_SIZE;

		return new Vec3i(cx, cy, cz);
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
		Chunk chunk = chunks.get(chunkPos);
		if (chunk != null && !chunk.pos.equals(chunkPos)) {
			throw new ValueMismatchException("Position of chunk received (" + chunk.pos + ") doesn't match the requested position (" + chunkPos + ")");
		}
		return chunk;
	}

	@Nullable
	public Chunk getChunkAt(int x, int y, int z) {
		return this.getChunkAt(new Vec3i(x, y, z));
	}

	@Nullable
	public Chunk getChunkAt(Vec3i pos) {
		int chunkX = Math.floorDiv(pos.x, CHUNK_SIZE);
		int chunkZ = Math.floorDiv(pos.z, CHUNK_SIZE);

		if (this.isOutOfWorldBounds(pos)) return null;

		ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
		return this.getChunk(chunkPos);
	}

	private boolean isOutOfWorldBounds(Vec3i pos) {
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
			this.set(x, maxY, z, block);
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

	public Collection<Chunk> getLoadedChunks() {
		return Collections.unmodifiableCollection(this.chunks.values());
	}

	public int getPlayTime() {
		return this.playTime;
	}

	/**
	 * <b>NOTE:</b> This method is obsolete, {@link #spawn(Entity, MapType)} exists with more functionality.
	 */
	@ApiStatus.Obsolete
	public <T extends Entity> T spawn(T entity) {
		Preconditions.checkNotNull(entity, "Cannot spawn null entity");
		this.setEntityId(entity);
		this.entities.put(entity.getId(), entity);
		return entity;
	}

	public <T extends Entity> T spawn(T entity, MapType spawnData) {
		Preconditions.checkNotNull(entity, "Cannot spawn null entity");
		Preconditions.checkNotNull(entity, "Cannot entity with nul spawn data");
		this.setEntityId(entity);
		entity.onPrepareSpawn(spawnData);
		this.entities.put(entity.getId(), entity);
		return entity;
	}

	private <T extends Entity> void setEntityId(T entity) {
		Preconditions.checkNotNull(entity, "Cannot set entity id for null entity");
		int oldId = entity.getId();
		if (oldId > 0 && this.entities.containsKey(oldId)) {
			throw new IllegalStateException("Entity already spawned: " + entity);
		}
		int newId = oldId > 0 ? oldId : this.nextId();
	}

	private int nextId() {
		return this.curId++;
	}

	public void despawn(Entity entity) {
		this.entities.remove(entity.getId());
	}

	public void despawn(int id) {
		this.entities.remove(id);
	}

	public Entity getEntity(int id) {
		return this.entities.get(id);
	}

	public List<BoundingBox> collide(BoundingBox box) {
		List<BoundingBox> boxes = new ArrayList<>();
		int xMin = (int) Math.floor(box.min.x);
		int xMax = (int) Math.floor(box.max.x);
		int yMin = (int) Math.floor(box.min.y);
		int yMax = (int) Math.floor(box.max.y);
		int zMin = (int) Math.floor(box.min.z);
		int zMax = (int) Math.floor(box.max.z);

		for (int x = xMin; x <= xMax; x++) {
			for (int y = yMin; y <= yMax; y++) {
				for (int z = zMin; z <= zMax; z++) {
					Block block = this.get(x, y, z);
					if (block.isSolid()) {
						BoundingBox blockBox = block.getBoundingBox(x, y, z);
						if (blockBox.intersects(box)) {
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

		ScheduledFuture<?> saveSchedule = this.saveSchedule;
		if (saveSchedule != null) saveSchedule.cancel(true);
		this.saveExecutor.shutdownNow();

		try {
			this.save(true);
		} catch (IOException e) {
			LOGGER.warn(MARKER, "Saving failed:", e);
		}

		for (Chunk chunk : this.chunks.values()) {
			chunk.dispose();
		}
		this.generator.dispose();
	}

	public CompletableFuture<Void> updateChunksForPlayerAsync(float spawnX, float spawnZ) {
		return this.updateChunksForPlayerAsync(new Vec3d(spawnX, 0, spawnZ));
	}

	@Deprecated
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

	public SavedWorld getSavedWorld() {
		return this.savedWorld;
	}

	@Nullable
	@Deprecated
	public WorldRegion getRegionAt(Vec3i blockPos) {
//		RegionPos regionPos = new RegionPos(Math.floorDiv(Math.floorDiv(blockPos.x, CHUNK_SIZE), REGION_SIZE), Math.floorDiv(Math.floorDiv(blockPos.z, CHUNK_SIZE), REGION_SIZE));
//		return this.getRegion(regionPos);
		return null;
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

	public Vec3i getSpawnPoint() {
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
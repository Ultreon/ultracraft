package com.ultreon.craft.server;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import com.ultreon.craft.api.commands.CommandSender;
import com.ultreon.craft.config.UltracraftServerConfig;
import com.ultreon.craft.debug.DebugFlags;
import com.ultreon.craft.debug.Debugger;
import com.ultreon.craft.debug.inspect.InspectionNode;
import com.ultreon.craft.debug.profiler.Profiler;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.gamerule.GameRules;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.ServerConnections;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.s2c.S2CAddPlayerPacket;
import com.ultreon.craft.network.packets.s2c.S2CRemovePlayerPacket;
import com.ultreon.craft.server.events.ServerLifecycleEvents;
import com.ultreon.craft.server.player.CacheablePlayer;
import com.ultreon.craft.server.player.CachedPlayer;
import com.ultreon.craft.server.player.PermissionMap;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.util.PollingExecutorService;
import com.ultreon.craft.util.Shutdownable;
import com.ultreon.craft.world.*;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.tuple.Pair;
import com.ultreon.libs.commons.v0.vector.Vec2d;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.crash.v0.CrashException;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.core.config.ConfigurationScheduler;
import org.apache.logging.log4j.core.util.WatchManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * The base class for the Ultracraft server.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@ApiStatus.NonExtendable
public abstract class UltracraftServer extends PollingExecutorService implements Runnable, Shutdownable {
    public static final int TPS = 20;
    public static final long NANOSECONDS_PER_SECOND = 1_000_000_000L;
    public static final long NANOSECONDS_PER_TICK = UltracraftServer.NANOSECONDS_PER_SECOND / UltracraftServer.TPS;

    public static final Logger LOGGER = LoggerFactory.getLogger("UltracraftServer");
    public static final String NAMESPACE = "ultracraft";
    private static final WatchManager WATCH_MANAGER = new WatchManager(new ConfigurationScheduler("Ultracraft"));
    private static UltracraftServer instance;
    private final List<ServerDisposable> disposables = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Queue<Pair<ServerPlayer, Supplier<Packet<? extends ClientPacketHandler>>>> chunkNetworkQueue = new ArrayDeque<>();
    private final Map<UUID, ServerPlayer> players = new ConcurrentHashMap<>();
    private final ServerConnections connections;
    private final WorldStorage storage;
    protected final InspectionNode<UltracraftServer> node;
    private final InspectionNode<Object> playersNode;
    protected ServerWorld world;
    protected int port;
    protected int renderDistance = 16;
    protected int entityRenderDistance = 6 * World.CHUNK_SIZE;
    private int chunkRefresh;
    private long onlineTicks;
    protected volatile boolean running = false;
    private int currentTps;
    private boolean sendingChunk;
    protected int maxPlayers = 10;
    private final Cache<String, CachedPlayer> cachedPlayers = CacheBuilder.newBuilder().expireAfterAccess(24, TimeUnit.HOURS).build();
    private final Map<Identifier, ? extends ServerWorld> worlds;
    private final GameRules gameRules = new GameRules();
    private final PermissionMap permissions = new PermissionMap();
    private CommandSender consoleSender = new ConsoleCommandSender();

    /**
     * Creates a new {@link UltracraftServer} instance.
     *
     * @param storage    the world storage for the world data.
     * @param parentNode the parent inspection node. (E.g., the client inspection node)
     */
    public UltracraftServer(WorldStorage storage, Profiler profiler, InspectionNode<?> parentNode) {
        super(profiler);

        this.storage = storage;

        UltracraftServer.instance = this;
        this.thread = new Thread(this, "server");

        this.connections = new ServerConnections(this);

        UltracraftServerConfig.get();

        MapType worldData = new MapType();
        if (this.storage.exists("world.ubo")) {
            try {
                worldData = this.storage.read("world.ubo");
            } catch (IOException e) {
                this.crash(e);
            }
        }

        this.world = new ServerWorld(this, this.storage, worldData);

        // TODO: Make dimension registry.
        this.worlds = Map.of(
                new Identifier("overworld"), this.world // Overworld dimension. TODO: Add more dimensions.
        );

        if (DebugFlags.INSPECTION_ENABLED) {
            this.node = parentNode.createNode("server", () -> this);
            this.playersNode = this.node.createNode("players", this.players::values);
            this.node.createNode("world", () -> this.world);
            this.node.create("refreshChunks", () -> this.chunkRefresh);
            this.node.create("renderDistance", () -> this.renderDistance);
            this.node.create("entityRenderDistance", () -> this.entityRenderDistance);
            this.node.create("maxPlayers", () -> this.maxPlayers);
            this.node.create("tps", () -> this.currentTps);
            this.node.create("onlineTicks", () -> this.onlineTicks);
        }
    }

    public static WatchManager getWatchManager() {
        return UltracraftServer.WATCH_MANAGER;
    }

    public void load() throws IOException {
        this.world.load();
    }

    public void save(boolean silent) throws IOException {
        try {
            this.world.save(silent);
        } catch (IOException e) {
            UltracraftServer.LOGGER.error("Failed to save world", e);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    /**
     * Submits a function to the server thread, and waits for it to complete.
     *
     * @param func the callable to be executed.
     * @return the result of the callable.
     * @param <T> the return type of the callable.
     */
    @CanIgnoreReturnValue
    public static <T> T invokeAndWait(@NotNull Callable<T> func) {
        return UltracraftServer.instance.submit(func).join();
    }

    /**
     * Submits a function to the server thread, and waits for it to complete.
     *
     * @param func the runnable to be executed.
     */
    public static void invokeAndWait(Runnable func) {
        UltracraftServer.instance.submit(func).join();
    }

    /**
     * Submits a function to the server thread, and returns a future.
     *
     * @param func the runnable to be executed.
     * @return the future.
     */
    @CanIgnoreReturnValue
    public static @NotNull CompletableFuture<Void> invoke(Runnable func) {
        return UltracraftServer.instance.submit(func);
    }

    /**
     * Submits a function to the server thread, and returns a future.
     *
     * @param func the callable to be executed.
     * @return the future.
     * @param <T> the return type of the callable.
     */
    @CanIgnoreReturnValue
    public static <T> @NotNull CompletableFuture<T> invoke(Callable<T> func) {
        return UltracraftServer.instance.submit(func);
    }

    /**
     * Starts the server.
     * Note: Internal API.
     * This should only be invoked when you know what you are doing.
     * Improper usage may result in memory leaks, crashes or corruptions.
     *
     * @throws IllegalStateException if the server is already running.
     */
    @ApiStatus.Internal
    public void start() {
        if (this.running) throw new IllegalStateException("Server already running!");
        this.running = true;
        this.thread.start();
    }

    /**
     * @return the currently running server.
     */
    public static UltracraftServer get() {
        return UltracraftServer.instance;
    }

    /**
     * @return true if the current thread is the server thread.
     */
    public static boolean isOnServerThread() {
        UltracraftServer instance = UltracraftServer.instance;
        if (instance == null) throw new IllegalStateException("Server closed!");
        return instance.thread.getId() == Thread.currentThread().getId();
    }

    @ApiStatus.Internal
    public void addPlayer(ServerPlayer player) {
        this.players.put(player.getUuid(), player);
    }

    /**
     * The server main loop.
     * Note: Internal API.
     */
    @Override
    @ApiStatus.Internal
    public final void run() {
        // Send server starting event to mods.
        ServerLifecycleEvents.SERVER_STARTING.factory().onServerStarting(this);

        var tickCap = 1000.0 / (double) UltracraftServer.TPS;
        var tickTime = 0d;
        var gameFrameTime = 0d;
        var ticksPassed = 0;

        double time = System.currentTimeMillis();

        try {
            // Send server started event to mods.
            ServerLifecycleEvents.SERVER_STARTED.factory().onServerStarted(this);

            //* Main-loop.
            while (this.running) {
                var canTick = false;

                double time2 = System.currentTimeMillis();
                var passed = time2 - time;
                gameFrameTime += passed;
                tickTime += passed;

                time = time2;

                while (gameFrameTime >= tickCap) {
                    gameFrameTime -= tickCap;

                    canTick = true;
                }

                // Check if we can tick.
                if (canTick) {
                    ticksPassed++;
                    try {
                        // Tick the server.
                        this.runTick();
                    } catch (Throwable t) {
                        this.crash(new Throwable("Game being ticked.", t));
                    }
                }

                // Calculate the current TPS every second.
                if (tickTime >= 1000.0d) {
                    this.currentTps = ticksPassed;
                    ticksPassed = 0;
                    tickTime = 0;
                }

                // Allow thread interrupting.
                Thread.sleep(1);
            }
        } catch (InterruptedException ignored) {

        } catch (Throwable t) {
            // Server crashed.
            this.crash(t);
            this.close();
            return;
        }

        // Close all connections.
        this.connections.stop();

        // Save all the server data.
        try {
            this.save(false);
        } catch (IOException e) {
            UltracraftServer.LOGGER.error("Saving server data failed!", e);
        }

        // Cleanup any resources allocated.
        this.players.clear();
        this.scheduler.shutdownNow();

        this.close();

        // Clear the instance.
        UltracraftServer.instance = null;

        // Send event for server stopping to mods.
        ServerLifecycleEvents.SERVER_STOPPED.factory().onServerStopped(this);
    }

    /**
     * Crashes the server thread, or the client if we are the integrated server.
     *
     * @param t the throwable that caused the crash.
     */
    public abstract void crash(Throwable t);

    /**
     * Stops the server thread in a clean state.
     * Note: this method is blocking.
     */
    @Override
    @Blocking
    public void shutdown() {
        // Send event for server stopping.
        ServerLifecycleEvents.SERVER_STOPPING.factory().onServerStopping(this);

        // Kick all the players and stop the connections.
        for (ServerPlayer player : this.getPlayers()) {
            player.kick("Server stopped");
        }

        // Set running flag to make server stop.
        this.running = false;

        try {
            this.thread.join(60000);
        } catch (InterruptedException e) {
            this.crash(new RuntimeException("Safe shutdown got interrupted."));
            Runtime.getRuntime().halt(1);
        }

        // Shut down the parent executor service.
        super.shutdownNow();
    }

    @OverridingMethodsMustInvokeSuper
    protected void runTick() {
        this.profiler.update();

        this.onlineTicks++;

        // Poll all the tasks in the queue.
        this.profiler.section("taskPolling", this::poll);

        // Tick connections.
        this.profiler.section("connections", this.connections::tick);

        // Tick the world.
        var world = this.world;
        if (world != null) {
            this.profiler.section("world", () -> {
                WorldEvents.PRE_TICK.factory().onPreTick(world);
                world.tick();
                WorldEvents.POST_TICK.factory().onPostTick(world);
            });
        }

        // Tick chunk refresh time.
        if (this.world != null && this.chunkRefresh-- <= 0) {
            this.chunkRefresh = UltracraftServer.seconds2ticks(0.5f);

            // Refresh chunks.
            ChunkRefresher refresher = new ChunkRefresher();
            for (ServerPlayer player : this.players.values()) {
                player.refreshChunks(refresher);
            }
            refresher.freeze();
            world.doRefresh(refresher);
        }

        // Poll the chunk network queue.
        this.profiler.section("chunkPackets", this::pollChunkPacket);
    }

    /**
     * Converts a time in seconds to ticks.
     *
     * @param seconds the time in seconds.
     * @return the number of ticks.
     */
    public static int seconds2ticks(float seconds) {
        return (int) (seconds * UltracraftServer.TPS);
    }

    /**
     * Converts a time in minutes to ticks.
     *
     * @param minutes the time in minutes.
     * @return the number of ticks.
     */
    public static int minutes2ticks(float minutes) {
        return (int) (minutes * 60 * UltracraftServer.TPS);
    }

    /**
     * Converts a time in hours to ticks.
     *
     * @param hours the time in hours.
     * @return the number of ticks.
     */
    public static int hours2ticks(float hours) {
        return (int) (hours * 3600 * UltracraftServer.TPS);
    }

    private void pollChunkPacket() {
        if (this.sendingChunk) return;

        Pair<ServerPlayer, Supplier<Packet<? extends ClientPacketHandler>>> poll = this.chunkNetworkQueue.poll();
        if (poll != null) {
            this.sendingChunk = true;
            ServerPlayer first = poll.getFirst();
            Packet<? extends ClientPacketHandler> second = poll.getSecond().get();
            first.connection.send(second);
        }
    }

    /**
     * Gets the number of ticks since the server started.
     *
     * @return the number of ticks since the server started.
     */
    public long getOnlineTicks() {
        return this.onlineTicks;
    }

    /**
     * Defer a server disposable to be disposed when the server is closed.
     *
     * @param disposable the server disposable.
     * @return the same server disposable.
     * @param <T> the type of the server disposable.
     */
    public <T extends ServerDisposable> T disposeOnClose(T disposable) {
        this.disposables.add(disposable);
        return disposable;
    }

    /**
     * Schedules a runnable to be executed after the specified delay.
     *
     * @param runnable the runnable.
     * @param time the delay.
     * @param unit the time unit of the delay.
     * @return the scheduled future.
     */
    public ScheduledFuture<?> schedule(Runnable runnable, long time, TimeUnit unit) {
        return this.scheduler.schedule(runnable, time, unit);
    }

    @Override
    public void close() {
        for (ServerDisposable disposable : this.disposables) {
            disposable.dispose();
        }

        this.world.dispose();

        this.scheduler.shutdown();

        try {
            if (!this.scheduler.awaitTermination(60, TimeUnit.SECONDS) && !this.scheduler.isTerminated()) {
                this.onTerminationFailed();
            }
        } catch (InterruptedException | CrashException exc) {
            this.crash(exc);
        }
    }

    /**
     * Handles the failed termination of the server.
     */
    protected abstract void onTerminationFailed();

    /**
     * @return the server port.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * @return the render distance.
     */
    public int getRenderDistance() {
        return this.renderDistance;
    }

    /**
     * @return the entity render distance.
     */
    public int getEntityRenderDistance() {
        return this.entityRenderDistance;
    }

    /**
     * @return the game's version.
     */
    public String getGameVersion() {
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(UltracraftServer.NAMESPACE);
        if (container.isEmpty()) throw new InternalError("Can't find mod container for the base game.");
        return container.get().getMetadata().getVersion().getFriendlyString();
    }

    /**
     * Get the player with the specified uuid.
     *
     * @param uuid the uuid of the player.
     * @return the player, or null if not found.
     */
    public @Nullable ServerPlayer getPlayer(UUID uuid) {
        return this.players.get(uuid);
    }

    /**
     * Get the player with the specified name.
     *
     * @param name the name of the player
     * @return the player, or null if not found.
     */
    public @Nullable ServerPlayer getPlayer(String name) {
        for (ServerPlayer player : this.players.values()) {
            if (player.getName().equals(name)) {
                return player;
            }
        }
        return null;
    }

    /**
     * Gets a player from the cache.
     *
     * @param name the name of the player
     * @return the player, or null if the player is not in the cache.
     */
    public @Nullable CachedPlayer getCachedPlayer(String name) {
        try {
            return this.cachedPlayers.get(name, () -> new CachedPlayer(null, name));
        } catch (ExecutionException e) {
            return null;
        }
    }

    /**
     * Gets a player from the cache.
     *
     * @param uuid the uuid of the player
     * @return the player, or null if the player is not in the cache.
     */
    public @Nullable CachedPlayer getCachedPlayer(UUID uuid) {
//        return this.cachedPlayers.get(name, () -> new CachedPlayer(uuid, null));
        return null;
    }

    /**
     * Places a player into the server.
     *
     * @param player the player to place.
     */
    @ApiStatus.Internal
    public void placePlayer(ServerPlayer player) {
        // Put the player into the player list.
        this.players.put(player.getUuid(), player);
        this.cachedPlayers.put(player.getName(), new CachedPlayer(player.getUuid(), player.getName()));

        if (DebugFlags.INSPECTION_ENABLED) {
            this.playersNode.createNode(player.getName(), () -> player);
        }

        // Send player to all other players within the render distance.
        var players = this.getPlayers()
                .stream()
                .toList();

        for (ServerPlayer other : players) {
            if (other == player) continue;
            Debugger.log("Player " + player.getName() + " is within the render distance of " + this.getEntityRenderDistance() + "!");
            other.connection.send(new S2CAddPlayerPacket(player.getUuid(), player.getName(), player.getPosition()));
            player.connection.send(new S2CAddPlayerPacket(other.getUuid(), other.getName(), other.getPosition()));
        }
    }

    /**
     * @return the server world.
     */
    public ServerWorld getWorld() {
        return this.world;
    }

    /**
     * @return the server connections.
     */
    public ServerConnections getConnections() {
        return this.connections;
    }

    /**
     * @return the world storage.
     */
    public WorldStorage getStorage() {
        return this.storage;
    }

    /**
     * @return all players in the server.
     */
    public Collection<ServerPlayer> getPlayers() {
        return this.players.values();
    }

    /**
     * Sends a chunk to all players that are within the render distance.
     *
     * @param globalPos the global position of the chunk.
     * @param chunk the chunk to send.
     * @throws IOException if an I/O error occurs.
     */
    public void sendChunk(ChunkPos globalPos, Chunk chunk) throws IOException {
        for (ServerPlayer player : this.players.values()) {
            Vec3d chunkPos3D = globalPos.getChunkOrigin().add(World.CHUNK_SIZE / 2f, World.CHUNK_HEIGHT / 2f, World.CHUNK_SIZE / 2f);
            Vec2d chunkPos2D = new Vec2d(chunkPos3D.x, chunkPos3D.z);
            Vec2d playerPos2D = new Vec2d(player.getX(), player.getZ());
            double dst = chunkPos2D.dst(playerPos2D);
            if (dst < this.getRenderDistance() * World.CHUNK_SIZE) {
                player.sendChunk(globalPos, chunk);
            }
        }
    }

    /**
     * @return the current TPS.
     */
    public int getCurrentTps() {
        return this.currentTps;
    }

    /**
     * Called when the player has disconnected from the server.
     *
     * @param player the player that disconnected.
     * @param message the disconnect message.
     */
    @ApiStatus.Internal
    public void onDisconnected(ServerPlayer player, String message) {
        UltracraftServer.LOGGER.info("Player '" + player.getName() + "' disconnected with message: " + message);
        this.players.remove(player.getUuid());
        for (ServerPlayer other : this.players.values()) {
            other.connection.send(new S2CRemovePlayerPacket(other.getUuid()));
        }
    }

    /**
     * Get the players in the specified chunk.
     *
     * @param pos the chunk to find players in.
     * @return the players in the specified chunk.
     */
    public Stream<ServerPlayer> getPlayersInChunk(ChunkPos pos) {
        return this.players.values().stream().filter(player -> player.getChunkPos().equals(pos));
    }

    /**
     * @return the maximum number of players configured in the server.
     */
    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    /**
     * @return the number of players currently in the server.
     */
    public int getPlayerCount() {
        return this.players.size();
    }

    public boolean isRunning() {
        return this.running;
    }

    public boolean isIntegrated() {
        return false;
    }

    public boolean isDedicated() {
        return !this.isIntegrated();
    }

    @Nullable
    public UUID getHost() {
        return null;
    }

    public ServerPlayer loadPlayer(String name, UUID uuid, Connection connection) {
        ServerPlayer player = new ServerPlayer(EntityTypes.PLAYER, this.world, uuid, name, connection);
        try {
            if (this.storage.exists("players/" + name + ".ubo")) {
                UltracraftServer.LOGGER.info("Loading player '" + name + "'...");
                MapType read = this.storage.read("players/" + name + ".ubo");
                player.load(read);
                player.markSpawned();
                player.markPlayedBefore();
                return player;
            }
        } catch (IOException e) {
            UltracraftServer.LOGGER.warn("Failed to load player '" + name + "'!", e);
        }

        return player;
    }

    public boolean hasPlayedBefore(CacheablePlayer player) {
        return this.storage.exists("players/" + player.getName() + ".ubo");
    }

    public void handleWorldSaveError(Exception e) {

    }

    public void handleChunkLoadFailure(ChunkPos globalPos, String reason) {

    }

    public ArrayList<CachedPlayer> getCachedPlayers() {
        return Lists.newArrayList();
    }

    public Collection<? extends World> getWorlds() {
        return Collections.unmodifiableCollection(this.worlds.values());
    }

    public GameRules getGameRules() {
        return this.gameRules;
    }

    public ServerWorld getWorld(Identifier name) {
        return this.worlds.get(name);
    }

    public PermissionMap getDefaultPermissions() {
        return this.permissions;
    }

    public @Nullable CacheablePlayer getCacheablePlayer(String name) {
        ServerPlayer player = this.getPlayer(name);
        if (player != null) return player;

        return this.getCachedPlayer(name);
    }

    public CacheablePlayer getCacheablePlayer(UUID uuid) {
        return null; // TODO: Implement cacheable players by uuid.
    }

    public @Nullable Entity getEntity(@NotNull UUID uuid) {
        return this.worlds.values().stream().map(World::getEntities).flatMap(Collection::stream).filter(entity -> entity.getUuid().equals(uuid)).findAny().orElse(null);
    }

    public CommandSender getConsoleSender() {
        return this.consoleSender;
    }
}

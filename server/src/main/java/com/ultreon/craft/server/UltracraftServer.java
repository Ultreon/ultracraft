package com.ultreon.craft.server;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.network.ServerConnections;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.s2c.S2CAddPlayerPacket;
import com.ultreon.craft.network.packets.s2c.S2CRemovePlayerPacket;
import com.ultreon.craft.server.events.ServerLifecycleEvents;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.util.PollingExecutorService;
import com.ultreon.craft.world.*;
import com.ultreon.libs.commons.v0.tuple.Pair;
import com.ultreon.libs.commons.v0.vector.Vec2d;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
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
 * @author XyperCode
 * @since 0.1.0
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@ApiStatus.NonExtendable
public abstract class UltracraftServer extends PollingExecutorService implements Runnable {
    public static final int TPS = 20;
    public static final long NANOSECONDS_PER_SECOND = 1_000_000_000L;
    public static final long NANOSECONDS_PER_TICK = UltracraftServer.NANOSECONDS_PER_SECOND / UltracraftServer.TPS;

    public static final Logger LOGGER = LoggerFactory.getLogger("UltracraftServer");
    private static UltracraftServer instance;
    private final List<ServerDisposable> disposables = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Queue<Pair<ServerPlayer, Supplier<Packet<? extends ClientPacketHandler>>>> chunkNetworkQueue = new ArrayDeque<>();
    private final Map<UUID, ServerPlayer> players = new ConcurrentHashMap<>();
    private final ServerConnections connections;
    private final WorldStorage storage;
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

    /**
     * Creates a new {@link UltracraftServer} instance.
     *
     * @param storage the world storage for the world data.
     */
    public UltracraftServer(WorldStorage storage) {
        super();
        this.storage = storage;

        UltracraftServer.instance = this;
        this.thread = new Thread(this, "server");

        this.connections = new ServerConnections(this);
        this.world = new ServerWorld(this, this.storage);
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

            //* Main loop.
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
        } catch (Throwable t) {
            // Server crashed.
            this.crash(t);
            this.close();
            return;
        }

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
    @Blocking
    public void shutdown() {
        // Only shutdown if we are on the server thread.
        if (!UltracraftServer.isOnServerThread()) {
            UltracraftServer.invoke(this::shutdown);
            return;
        }

        // Send event for server stopping.
        ServerLifecycleEvents.SERVER_STOPPING.factory().onServerStopping(this);

        // Kick all the players and stop the connections.
        for (ServerPlayer player : this.getPlayers()) {
            player.kick("Server stopped");
        }

        this.connections.stop();

        // Set running flag to make server stop.
        this.running = false;

        // Cleanup any resources allocated.
        this.players.clear();
        this.world.dispose();
        this.scheduler.shutdownNow();

        // Stop the server thread.
        this.thread.interrupt();

        try {
            this.thread.join(30000);
            if (this.thread.isAlive()) {
                this.crash(new RuntimeException("Server thread did not terminate in 30 seconds!"));
                Runtime.getRuntime().halt(1);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Clear the instance.
        UltracraftServer.instance = null;

        // Shut down the parent executor service.
        super.shutdown();
    }

    private void runTick() {
        this.onlineTicks++;

        // Poll all the tasks in the queue.
        this.pollAll();

        // Tick connections.
        this.connections.tick();

        // Tick the world.
        var world = this.world;
        if (world != null) {
            WorldEvents.PRE_TICK.factory().onPreTick(world);
            world.tick();
            WorldEvents.POST_TICK.factory().onPostTick(world);
        }

        // Tick chunk refresh time.
        if (this.world != null && this.chunkRefresh-- <= 0) {
            this.chunkRefresh = UltracraftServer.seconds2ticks(2);

            // Refresh chunks.
            ChunkRefresher refresher = new ChunkRefresher();
            for (ServerPlayer player : this.players.values()) {
                player.refreshChunks(refresher);
            }
            refresher.freeze();
            world.doRefresh(refresher);
        }

        // Poll the chunk network queue.
        this.pollChunkPacket();
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

    private void close() {
        for (ServerDisposable disposable : this.disposables) {
            disposable.dispose();
        }

        try {
            if (!this.scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                this.onTerminationFailed();
            }
        } catch (InterruptedException exc) {
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
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(ServerConstants.NAMESPACE);
        if (container.isEmpty()) throw new InternalError("Can't find mod container for the base game.");
        return container.get().getMetadata().getVersion().getFriendlyString();
    }

    /**
     * Get the player with the specified uuid.
     *
     * @param uuid the uuid of the player.
     * @return the player, or null if not found.
     */
    public @Nullable ServerPlayer getPlayerByUuid(UUID uuid) {
        return this.players.get(uuid);
    }

    /**
     * Get the player with the specified name.
     *
     * @param name the name of the player
     * @return the player, or null if not found.
     */
    public @Nullable ServerPlayer getPlayerByName(String name) {
        for (ServerPlayer player : this.players.values()) {
            if (player.getName().equals(name)) {
                return player;
            }
        }
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

        // Send player to all other players within the render distance.
        var players = this.getPlayers()
                .stream()
                .toList();

        for (ServerPlayer other : players) {
            if (other == player) continue;
            System.out.println("Player " + player.getName() + " is within the render distance of " + this.getEntityRenderDistance() + "!");
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

    private void _sendChunk(ServerPlayer player, ChunkPos pos, Chunk chunk) {
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
        return running;
    }

    public boolean isIntegrated() {
        return false;
    }

    @Nullable
    public UUID getHost() {
        return null;
    }
}

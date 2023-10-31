package com.ultreon.craft.server;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.entity.Entity;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.network.PacketResult;
import com.ultreon.craft.network.ServerConnections;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.s2c.S2CPlayerPositionPacket;
import com.ultreon.craft.network.packets.s2c.S2CChunkDataPacket;
import com.ultreon.craft.server.events.ServerLifecycleEvents;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.util.PollingExecutorService;
import com.ultreon.craft.world.*;
import com.ultreon.libs.commons.v0.tuple.Pair;
import com.ultreon.libs.commons.v0.vector.Vec2d;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.QuiltLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
    private final Map<UUID, ServerPlayer> players = new HashMap<>();
    private final ServerConnections connection;
    private final WorldStorage storage;
    protected ServerWorld world;
    protected int port;
    protected int renderDistance = 8;
    protected int entityRenderDistance = 6;
    private int chunkRefresh;
    private long onlineTicks;
    protected volatile boolean running = true;
    private int currentTps;
    private boolean sendingChunk;
    protected int maxPlayers = 10;

    public UltracraftServer(WorldStorage storage) {
        super();
        this.storage = storage;

        UltracraftServer.instance = this;
        this.thread = new Thread(this, "server");

        this.connection = new ServerConnections(this);
        this.world = new ServerWorld(this, this.storage);
    }

    @CanIgnoreReturnValue
    public static <T> T invokeAndWait(@NotNull Callable<T> func) {
        return UltracraftServer.instance.submit(func).join();
    }

    public static void invokeAndWait(Runnable func) {
        UltracraftServer.instance.submit(func).join();
    }

    @CanIgnoreReturnValue
    public static @NotNull CompletableFuture<Void> invoke(Runnable func) {
        return UltracraftServer.instance.submit(func);
    }

    @CanIgnoreReturnValue
    public static <T> @NotNull CompletableFuture<T> invoke(Callable<T> func) {
        return UltracraftServer.instance.submit(func);
    }

    public void start() {
        this.thread.start();
    }

    public static UltracraftServer get() {
        return UltracraftServer.instance;
    }

    public static boolean isOnServerThread() {
        UltracraftServer instance = UltracraftServer.instance;
        if (instance == null) throw new IllegalStateException("Server closed!");
        return instance.thread.getId() == Thread.currentThread().getId();
    }

    @ApiStatus.Internal
    public void addPlayer(ServerPlayer player) {
        this.players.put(player.getUuid(), player);
    }

    public void run() {
        ServerLifecycleEvents.SERVER_STARTING.factory().onServerStarting(this);

        var tickCap = 1000.0 / (double) UltracraftServer.TPS;
        var tickTime = 0d;
        var gameFrameTime = 0d;
        var ticksPassed = 0;

        double time = System.currentTimeMillis();

        try {
            ServerLifecycleEvents.SERVER_STARTED.factory().onServerStarted(this);
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

                if (canTick) {
                    ticksPassed++;
                    try {
                        this.runTick();
                    } catch (Throwable t) {
                        this.crash(new Throwable("Game being ticked.", t));
                    }
                }

                if (tickTime >= 1000.0d) {
                    this.currentTps = ticksPassed;
                    ticksPassed = 0;
                    tickTime = 0;
                }

                Thread.sleep(1);
            }
        } catch (Throwable t) {
            this.crash(t);
            this.close();
            return;
        }

        ServerLifecycleEvents.SERVER_STOPPED.factory().onServerStopped(this);
    }

    public abstract void crash(Throwable t);

    public void shutdown() {
        if (!UltracraftServer.isOnServerThread()) {
            UltracraftServer.invoke(this::shutdown);
            return;
        }

        ServerLifecycleEvents.SERVER_STOPPING.factory().onServerStopping(this);

        this.connection.stop();

        this.running = false;

        for (ServerPlayer player : this.players.values()) {
            player.kick("Server stopped");
        }

        this.players.clear();
        this.world.dispose();

        try {
            this.thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        UltracraftServer.instance = null;

        super.shutdown();
    }

    private void runTick() {
        this.onlineTicks++;

        this.pollAll();

        this.connection.tick();

        var world = this.world;
        if (world != null) {
            WorldEvents.PRE_TICK.factory().onPreTick(world);
            world.tick();
            WorldEvents.POST_TICK.factory().onPostTick(world);
        }

        // Tick chunk refresh time.
        if (this.world != null && this.chunkRefresh-- <= 0) {
            this.chunkRefresh = UltracraftServer.seconds2tick(2);

            // Refresh chunks.
            ChunkRefresher refresher = new ChunkRefresher();
            for (ServerPlayer player : this.players.values()) {
                player.refreshChunks(refresher);
            }
            refresher.freeze();
            world.doRefresh(refresher);
        }

        this.pollChunkPacket();
    }

    public static int seconds2tick(float seconds) {
        return (int) (seconds * UltracraftServer.TPS);
    }

    public static int minutes2tick(float minutes) {
        return (int) (minutes * 60 * UltracraftServer.TPS);
    }

    public static int hours2tick(float hours) {
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

    public long getOnlineTicks() {
        return this.onlineTicks;
    }

    public <T extends ServerDisposable> T disposeOnClose(T disposable) {
        this.disposables.add(disposable);
        return disposable;
    }

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

    protected abstract void onTerminationFailed();

    public int getPort() {
        return this.port;
    }

    public int getRenderDistance() {
        return this.renderDistance;
    }

    public int getEntityRenderDistance() {
        return this.entityRenderDistance;
    }

    public String getGameVersion() {
        return QuiltLoader.getNormalizedGameVersion();
    }

    public ServerPlayer getPlayerByUuid(UUID uuid) {
        return this.players.get(uuid);
    }

    public ServerPlayer getPlayerByName(String name) {
        for (ServerPlayer player : this.players.values()) {
            if (player.getName().equals(name)) {
                return player;
            }
        }
        return null;
    }

    public void placePlayer(ServerPlayer player) {
        this.players.put(player.getUuid(), player);
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public ServerConnections getConnection() {
        return this.connection;
    }

    public WorldStorage getStorage() {
        return this.storage;
    }

    public Collection<ServerPlayer> getPlayers() {
        return this.players.values();
    }

    public void sendChunk(ChunkPos globalPos, Chunk chunk) throws IOException {
        for (ServerPlayer player : this.players.values()) {
            Vec3d chunkPos3D = globalPos.getChunkOrigin().add(World.CHUNK_SIZE / 2f, World.CHUNK_HEIGHT / 2f, World.CHUNK_SIZE / 2f);
            Vec2d chunkPos2D = new Vec2d(chunkPos3D.x, chunkPos3D.z);
            Vec2d playerPos2D = new Vec2d(player.getX(), player.getZ());
            double dst = chunkPos2D.dst(playerPos2D);
            if (dst < this.getRenderDistance() * World.CHUNK_SIZE) {
                this._sendChunk(player, globalPos, chunk);
            }
        }
    }

    private void _sendChunk(ServerPlayer player, ChunkPos pos, Chunk chunk) {
        player.onChunkPending(pos);
        player.connection.send(new S2CChunkDataPacket(pos, ArrayUtils.clone(chunk.storage.getPalette()), new ArrayList<>(chunk.storage.getData())), PacketResult.onEither(() -> this.sendingChunk = false));

        List<Vec3d> list = this.players.values().stream().map(Entity::getPosition).filter(position -> position.dst(player.getPosition()) < this.entityRenderDistance).toList();
        player.connection.send(new S2CPlayerPositionPacket(list));
        player.connection.send(new S2CChunkDataPacket(pos, ArrayUtils.clone(chunk.storage.getPalette()), new ArrayList<>(chunk.storage.getData())), PacketResult.onEither(() -> this.sendingChunk = false));
    }

    public int getCurrentTps() {
        return this.currentTps;
    }

    public void onDisconnected(ServerPlayer player, String message) {
        UltracraftServer.LOGGER.info("Player '" + player.getName() + "' disconnected with message: " + message);
    }

    public Stream<ServerPlayer> getPlayersInChunk(ChunkPos pos) {
        return this.players.values().stream().filter(player -> player.getChunkPos().equals(pos));
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public int getPlayerCount() {
        return this.players.size();
    }
}

package com.ultreon.craft.server.player;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.packets.S2CChunkCancelPacket;
import com.ultreon.craft.network.packets.s2c.S2CPlayerHealthPacket;
import com.ultreon.craft.network.packets.s2c.S2CPlayerSetPosPacket;
import com.ultreon.craft.network.packets.s2c.S2CRespawnPacket;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.Unit;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.ChunkRefresher;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class ServerPlayer extends Player {
    public Connection connection;
    private final ServerWorld world;
    private UUID uuid;
    private final String name;
    private final UltracraftServer server = UltracraftServer.get();
    private final Object2IntMap<ChunkPos> retryChunks = Object2IntMaps.synchronize(new Object2IntArrayMap<>());
    private final Cache<ChunkPos, S2CChunkCancelPacket> pendingChunks = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).removalListener(notification -> { }).build();
    private final Cache<ChunkPos, Unit> failedChunks = CacheBuilder.newBuilder().expireAfterWrite(90, TimeUnit.SECONDS).build();
    private final Set<ChunkPos> activeChunks = new HashSet<>();

    public ServerPlayer(EntityType<? extends Player> entityType, ServerWorld world, UUID uuid, String name) {
        super(entityType, world);
        this.world = world;
        this.uuid = uuid;
        this.name = name;
    }

    public void kick(String kick) {
        this.connection.disconnect(kick);
    }

    public void respawn() {
        assert this.world != null;
        if (this.world.getEntity(this.getId()) == this) {
            this.world.despawn(this);
        }

        this.world.despawn(this);
        try {
            var spawnPoint = this.server.submit(this.world::getSpawnPoint).join();

            Vec3d spawnAt = spawnPoint.vec().d().add(0.5, 0, 0.5);
            this.setPosition(spawnAt);
            this.health = this.getMaxHeath();
            this.isDead = false;
            this.damageImmunity = 40;
            this.spawn(spawnAt, this.connection);
        } catch (Exception e) {
            UltracraftServer.LOGGER.error("Failed to spawn player!", e);
        }
    }

    @Override
    public boolean onHurt(float damage, @NotNull DamageSource source) {
        if (this.damageImmunity > 0) return true;
        return super.onHurt(damage, source);
    }

    @ApiStatus.Internal
    public void spawn(Vec3d position, Connection connection) {
        Preconditions.checkNotNull(position, "position");
        Preconditions.checkNotNull(connection, "connection");

        this.connection = connection;
        this.setHealth(this.getMaxHeath());
        this.setPosition(position);
        this.world.prepareSpawn(this);
        this.world.spawn(this);
        this.connection.send(new S2CRespawnPacket(this.getPosition()));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.oldHealth != this.health) {
            this.connection.send(new S2CPlayerHealthPacket(this.health));
            this.oldHealth = this.health;
        }
    }

    @Override
    protected void move() {

    }

    @Override
    protected void onMoved() {
        super.onMoved();

        this.connection.send(new S2CPlayerSetPosPacket(this.getPosition()));
    }

    @Override
    public @NotNull UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    @Override
    protected void setUuid(@NotNull UUID uuid) {
        if (this.uuid != null) throw new IllegalStateException("Uuid already set!");
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "ServerPlayer{" +
                "uuid=" + this.uuid +
                ", name='" + this.name + '\'' +
                '}';
    }

    public void onChunkStatus(@NotNull ChunkPos pos, Chunk.Status status) {
        switch (status) {
            case FAILED -> {
                if (this.retryChunks.computeInt(pos, (chunkPos, integer) -> integer == null ? 1 : integer + 1) == 3)
                    this.pendingChunks.invalidate(pos);
            }
            case SUCCESS -> this.activeChunks.add(pos);
        }
    }

    public void onChunkPending(ChunkPos pos) {
        this.pendingChunks.put(pos, new S2CChunkCancelPacket(pos));
    }

    public void refreshChunks(ChunkRefresher refresher) {
        Vec3d pos = this.getPosition();
        var needed = this.world.getChunksAround(pos);
        var toLoad = this.getChunksToLoad(needed);
        var toUnload = this.getChunksToUnload(needed);
        this.pendingChunks.invalidateAll(toUnload);

        refresher.addLoading(toLoad);
        refresher.addUnloading(toUnload);
    }

    private List<ChunkPos> getChunksToUnload(List<ChunkPos> needed) {
        return this.activeChunks.stream()
                .filter(pos -> !needed.contains(pos))
                .toList();
    }

    private List<ChunkPos> getChunksToLoad(List<ChunkPos> needed) {
        return needed.stream()
                .filter(chunkPos -> !this.activeChunks.contains(chunkPos))
                .toList();
    }
}

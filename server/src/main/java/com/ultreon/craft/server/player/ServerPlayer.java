package com.ultreon.craft.server.player;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ultreon.craft.collection.PaletteStorage;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.packets.s2c.S2CChunkCancelPacket;
import com.ultreon.craft.network.packets.s2c.S2CPlayerHealthPacket;
import com.ultreon.craft.network.packets.s2c.S2CPlayerSetPosPacket;
import com.ultreon.craft.network.packets.s2c.S2CRespawnPacket;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.Unit;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.ChunkRefresher;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.libs.commons.v0.vector.Vec2d;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class ServerPlayer extends Player {
    public Connection connection;
    private final ServerWorld world;
    private UUID uuid;
    private final String name;
    private final UltracraftServer server = UltracraftServer.get();
    private final Object2IntMap<ChunkPos> retryChunks = Object2IntMaps.synchronize(new Object2IntArrayMap<>());
    private final Cache<ChunkPos, S2CChunkCancelPacket> pendingChunks = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).removalListener(notification -> { }).build();
    private final Cache<ChunkPos, Unit> failedChunks = CacheBuilder.newBuilder().expireAfterWrite(90, TimeUnit.SECONDS).removalListener(notification -> {
    }).build();
    private final Set<ChunkPos> activeChunks = new HashSet<>();
    private final Set<ChunkPos> skippedChunks = new HashSet<>();
    private ChunkPos oldChunkPos = new ChunkPos(0, 0);

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

        // Send new position to client.
        if (this.world.getChunk(this.getChunkPos()) == null) {
            this.setPosition(this.ox, this.oy, this.oz);
            this.connection.send(new S2CPlayerSetPosPacket(this.getPosition()));
        }

        // Limit player speed server-side.
        double maxDistance = (this.isFlying() ? this.getFlyingSpeed() : this.getWalkingSpeed()) * 3;
        if (this.getPosition().dst(this.ox, this.oy, this.oz) > maxDistance) {
            UltracraftServer.LOGGER.warn("Player moved too quickly: " + this.getName() + " (distance: " + this.getPosition().dst(this.ox, this.oy, this.oz) + ", max: " + maxDistance + ")");
            this.teleportTo(this.ox, this.oy, this.oz);
        }

        // Set old position.
        this.ox = this.x;
        this.oy = this.y;
        this.oz = this.z;
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
                if (this.retryChunks.computeInt(pos, (chunkPos, integer) -> integer == null ? 1 : integer + 1) == 3) {
                    this.pendingChunks.invalidate(pos);
                    this.failedChunks.put(pos, Unit.INSTANCE);
                }
            }
            case SKIP -> this.skippedChunks.add(pos);
            case SUCCESS -> this.activeChunks.add(pos);
        }
    }

    public void onChunkPending(ChunkPos pos) {
        this.pendingChunks.put(pos, new S2CChunkCancelPacket(pos));
    }

    public void refreshChunks(ChunkRefresher refresher) {
        var pos = this.getPosition();
        var chunkPos = this.getChunkPos();
        var needed = this.world.getChunksAround(pos);
        var toLoad = this.getChunksToLoad(needed);
        var toUnload = this.getChunksToUnload(needed);

        // Invalidate all pending chunks that are to be unloaded.
        this.pendingChunks.invalidateAll(toUnload);

        // Remove skipped chunks if the player didn't move in between chunks.
        if (this.oldChunkPos.equals(chunkPos)) toLoad.removeAll(this.skippedChunks);
        else this.oldChunkPos = chunkPos;

        // Remove all failed chunks.
        toLoad.removeAll(this.failedChunks.asMap().keySet());

        // Add all loading/unloading chunks.
        refresher.addLoading(toLoad.stream().sorted((o1, o2) -> {
            // Compare against player position.
            Vec2d playerPos = new Vec2d(chunkPos.x(), chunkPos.z());
            Vec2d cPos1 = new Vec2d(o1.x(), o1.z());
            Vec2d cPos2 = new Vec2d(o2.x(), o2.z());

            return Double.compare(cPos1.dst(playerPos), cPos2.dst(playerPos));
        }).toList());
        refresher.addUnloading(toUnload);
    }

    private ListOrderedSet<ChunkPos> getChunksToUnload(List<ChunkPos> needed) {
        return this.activeChunks.stream()
                .filter(pos -> !needed.contains(pos))
                .collect(Collectors.toCollection(ListOrderedSet::new));
    }

    private ListOrderedSet<ChunkPos> getChunksToLoad(List<ChunkPos> needed) {
        return needed.stream()
                .filter(chunkPos -> !this.activeChunks.contains(chunkPos))
                .collect(Collectors.toCollection(ListOrderedSet::new));
    }

    public void teleportTo(int x, int y, int z) {
        this.setPosition(x + 0.5, y, z + 0.5);
        this.connection.send(new S2CPlayerSetPosPacket(x + 0.5, y, z + 0.5));
    }

    public void teleportTo(double x, double y, double z) {
        this.setPosition(x, y, z);
        this.connection.send(new S2CPlayerSetPosPacket(x, y, z));
    }
}

package com.ultreon.craft.server.player;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.events.PlayerEvents;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.menu.ContainerMenu;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.PacketResult;
import com.ultreon.craft.network.packets.s2c.*;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.Unit;
import com.ultreon.craft.world.*;
import com.ultreon.libs.commons.v0.vector.Vec2d;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Server-side player implementation.
 * Represents an online player.
 * <p style="color: red;">NOTE: Should not be stored in collections, if you need to store a player in a collection. You should get the cached player instead,</p>
 *
 * @see UltracraftServer#getCachedPlayer(String)
 */
public non-sealed class ServerPlayer extends Player implements CacheablePlayer {
    public Connection connection;
    private final ServerWorld world;
    public int hotbarIdx;
    private UUID uuid;
    private final String name;
    private final UltracraftServer server = UltracraftServer.get();
    private final Object2IntMap<ChunkPos> retryChunks = Object2IntMaps.synchronize(new Object2IntArrayMap<>());
    private final Cache<ChunkPos, S2CChunkCancelPacket> pendingChunks = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).removalListener(notification -> { }).build();
    private final Cache<ChunkPos, Unit> failedChunks = CacheBuilder.newBuilder().expireAfterWrite(90, TimeUnit.SECONDS).removalListener(notification -> {
    }).build();
    private final Set<ChunkPos> activeChunks = new CopyOnWriteArraySet<>();
    private final Set<ChunkPos> skippedChunks = new CopyOnWriteArraySet<>();
    private ChunkPos oldChunkPos = new ChunkPos(0, 0);
    private boolean sendingChunk;
    private boolean spawned;
    private boolean playedBefore;

    public ServerPlayer(EntityType<? extends Player> entityType, ServerWorld world, UUID uuid, String name, Connection connection) {
        super(entityType, world);
        this.world = world;
        this.uuid = uuid;
        this.name = name;

        this.connection = connection;
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

    /**
     * @param damage the damage dealt.
     * @param source the source of the damage.
     * @return true to cancel the damage.
     */
    @Override
    public boolean onHurt(float damage, @NotNull DamageSource source) {
        if (this.damageImmunity > 0) return true;
        boolean doDamage = super.onHurt(damage, source);
        if (!doDamage) this.playSound(this.getHurtSound(), 1.0f);
        return doDamage;
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

        this.spawned = true;
    }

    public void markSpawned() {
        this.spawned = true;
    }

    @Override
    public void tick() {
        if (this.world.getChunk(this.getChunkPos()) == null) return;

        if (!this.isChunkActive(this.getChunkPos())) {
            return;
        }

        super.tick();

        if (this.oldHealth != this.health) {
            this.connection.send(new S2CPlayerHealthPacket(this.health));
            this.oldHealth = this.health;
        }

        ContainerMenu menu = this.getOpenMenu();
        if (menu != null) {
            BlockPos pos = menu.getPos();
            if (pos != null && pos.vec().d().dst(this.getPosition()) > 5)
                this.autoCloseMenu();
        }
    }

    private void autoCloseMenu() {
        this.connection.send(new S2CCloseContainerMenuPacket());
    }

    @Override
    protected void move() {

    }

    @Override
    protected void onMoved() {
        super.onMoved();

        // Send the new position to the client.
        if (this.world.getChunk(this.getChunkPos()) == null) {
            this.setPosition(this.ox, this.oy, this.oz);
            this.connection.send(new S2CPlayerSetPosPacket(this.getPosition()));
        }

        if (Math.abs(this.x - this.ox) < 0.001 &&
                Math.abs(this.y - this.oy) < 0.001 &&
                Math.abs(this.z - this.oz) < 0.001)
            return;

        // Limit player speed server-side.
        double maxDistanceXZ = (this.isFlying() ? this.getFlyingSpeed() : this.getWalkingSpeed()) * 12.5;
        double maxDistanceY = (this.isFlying() ? this.getFlyingSpeed() : this.getWalkingSpeed() * 5) * Math.max(this.fallDistance * this.gravity, 2);
        if (this.getPosition().dst(this.ox, this.y, this.oz) > maxDistanceXZ) {
            UltracraftServer.LOGGER.warn("Player moved too quickly: " + this.getName() + " (distance: " + this.getPosition().dst(this.ox, this.oy, this.oz) + ", max xz: " + maxDistanceXZ + ")");
//            this.teleportTo(this.ox, this.oy, this.oz);
        }
        if (Math.abs(this.getY() - this.oy) > maxDistanceY) {
            UltracraftServer.LOGGER.warn("Player moved too quickly: " + this.getName() + " (distance: " + this.getPosition().dst(this.ox, this.oy, this.oz) + ", max y: " + maxDistanceY + ")");
//            this.teleportTo(this.ox, this.oy, this.oz);
        }

        // Set old position.
        this.ox = this.x;
        this.oy = this.y;
        this.oz = this.z;

        for (var player : this.server.getPlayers()) {
            if (player == this) continue;

            if (player.getPosition().dst(this.getPosition()) < this.server.getEntityRenderDistance()) {
                player.connection.send(new S2CPlayerPositionPacket(this.getUuid(), this.getPosition()));
            }
        }
    }

    @Override
    public @NotNull UUID getUuid() {
        return this.uuid;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public boolean isCache() {
        return false;
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
        return "ServerPlayer{'" + this.name + "'}";
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
            case UNLOADED -> this.activeChunks.remove(pos);
        }
    }

    public void onChunkPending(ChunkPos pos) {
        this.pendingChunks.put(pos, new S2CChunkCancelPacket(pos));
    }

    public void refreshChunks(ChunkRefresher refresher) {
        var pos = this.getPosition();
        var chunkPos = this.getChunkPos();
        var server = this.server;
        var world = this.world;
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
        toLoad.removeAll(toUnload);

        ServerPlayer.refreshChunks(refresher, server, world, chunkPos, toLoad, toUnload);
    }

    @ApiStatus.Internal
    public static void refreshChunks(ChunkRefresher refresher, UltracraftServer server, ServerWorld world, ChunkPos chunkPos, ListOrderedSet<ChunkPos> toLoad, ListOrderedSet<ChunkPos> toUnload) {
        List<ChunkPos> load = toLoad.stream().sorted((o1, o2) -> {
            // Compare against player position.
            Vec2d playerPos = new Vec2d(chunkPos.x(), chunkPos.z());
            Vec2d cPos1 = new Vec2d(o1.x(), o1.z());
            Vec2d cPos2 = new Vec2d(o2.x(), o2.z());

            return Double.compare(cPos1.dst(playerPos), cPos2.dst(playerPos));
        }).toList();

        for (ChunkPos loadingChunk : load) {
            ServerChunk chunk = world.getChunk(loadingChunk);
            if (chunk != null) {
                try {
                    server.sendChunk(loadingChunk, chunk);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // Add all loading/unloading chunks.
        refresher.addLoading(load);
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

    public void sendChunk(ChunkPos pos, Chunk chunk) {
        if (this.sendingChunk) return;

        this.onChunkPending(pos);
        this.connection.send(new S2CChunkDataPacket(pos, ArrayUtils.clone(chunk.storage.getPalette()), new ArrayList<>(chunk.storage.getData())), PacketResult.onEither(() -> this.sendingChunk = false));
    }

    @Override
    public void playSound(@Nullable SoundEvent sound, float volume) {
        if (sound == null) return;
        this.connection.send(new S2CPlaySoundPacket(sound.getId(), volume));
    }

    @Override
    public void openMenu(@NotNull ContainerMenu menu) {
        super.openMenu(menu);

        this.connection.send(new S2COpenContainerMenuPacket(this.inventory.getType().getId()));
    }

    @Override
    public void setCursor(@NotNull ItemStack cursor) {
        this.connection.send(new S2CMenuCursorPacket(cursor));
        super.setCursor(cursor);
    }

    @Override
    public @NotNull ServerWorld getWorld() {
        return this.world;
    }

    public Vec2i getChunkVec() {
        return World.toChunkVec(this.blockPosition());
    }

    public boolean isChunkActive(ChunkPos chunkPos) {
        return this.activeChunks.contains(chunkPos);
    }

    public void setInitialItems() {
        if (PlayerEvents.INITIAL_ITEMS.factory().onPlayerInitialItems(this, this.inventory).isCanceled()) {
            return;
        }

        this.inventory.addItem(Items.WOODEN_PICKAXE.defaultStack());
        this.inventory.addItem(Items.WOODEN_SHOVEL.defaultStack());
    }

    public void handlePlayerMove(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean isSpawned() {
        return this.spawned;
    }

    public void markPlayedBefore() {
        this.playedBefore = true;
    }

    public boolean hasPlayedBefore() {
        return this.playedBefore;
    }
}

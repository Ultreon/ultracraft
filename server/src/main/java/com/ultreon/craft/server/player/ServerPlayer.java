package com.ultreon.craft.server.player;

import com.google.common.base.Preconditions;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.entity.damagesource.DamageSource;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.packets.s2c.S2CPlayerHealthPacket;
import com.ultreon.craft.network.packets.s2c.S2CPlayerSetPosPacket;
import com.ultreon.craft.network.packets.s2c.S2CRespawnPacket;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class ServerPlayer extends Player {
    public Connection connection;
    private final ServerWorld world;
    private UUID uuid;
    private final String name;
    private final UltracraftServer server = UltracraftServer.get();

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
        this.server.submit(() -> this.world.refreshChunks(this)).join();
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
}

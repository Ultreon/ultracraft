package com.ultreon.craft.network;

import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.util.Env;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PacketContext {
    private final @Nullable ServerPlayer player;
    private final @NotNull Connection connection;
    private final @NotNull Env destination;

    public PacketContext(@Nullable ServerPlayer player, @NotNull Connection connection, @NotNull Env environment) {
        this.player = player;
        this.connection = connection;
        this.destination = environment;
    }

    public void queue(Runnable handler) {
        this.connection.queue(handler);
    }

    public @Nullable ServerPlayer getPlayer() {
        return this.player;
    }

    public @NotNull Connection getConnection() {
        return this.connection;
    }

    public @NotNull Env getDestination() {
        return this.destination;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PacketContext) obj;
        return Objects.equals(this.player, that.player) &&
                Objects.equals(this.connection, that.connection) &&
                Objects.equals(this.destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.player, this.connection, this.destination);
    }

    @Override
    public String toString() {
        return "PacketContext[" +
                "player=" + this.player + ", " +
                "connection=" + this.connection + ", " +
                "environment=" + this.destination + ']';
    }

    public @NotNull ServerPlayer requirePlayer() {
        ServerPlayer player = this.player;
        if (player == null) throw new PacketException("Packet handling requires player, but there's no player in this context.");
        return player;
    }
}

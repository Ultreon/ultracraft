package com.ultreon.craft.network.server;

import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.NetworkChannel;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.PacketResult;
import com.ultreon.craft.network.api.packet.ModPacket;
import com.ultreon.craft.network.api.packet.ModPacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.s2c.S2CLoginAcceptedPacket;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.libs.commons.v0.Identifier;
import net.fabricmc.api.EnvType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginServerPacketHandler implements ServerPacketHandler {
    private static final Map<Identifier, NetworkChannel> CHANNELS = new HashMap<>();
    private final UltracraftServer server;
    private final Connection connection;
    private final PacketContext context;

    public LoginServerPacketHandler(UltracraftServer server, Connection connection) {
        this.server = server;
        this.connection = connection;
        this.context = new PacketContext(null, connection, EnvType.SERVER);
    }

    public static NetworkChannel registerChannel(Identifier id) {
        NetworkChannel channel = NetworkChannel.create(id);
        LoginServerPacketHandler.CHANNELS.put(id, channel);
        return channel;
    }

    @Override
    public void onDisconnect(String message) {
        this.connection.close();
    }

    public boolean shouldHandlePacket(Packet<?> packet) {
        if (ServerPacketHandler.super.shouldHandlePacket(packet)) return true;
        else return this.connection.isConnected();
    }

    @Override
    public PacketContext context() {
        return this.context;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isAcceptingPackets() {
        return this.connection.isConnected();
    }

    public void onModPacket(NetworkChannel channel, ModPacket<?> packet) {
        packet.handlePacket(() -> new ModPacketContext(channel, null, this.connection, EnvType.SERVER));
    }

    public NetworkChannel getChannel(Identifier channelId) {
        return LoginServerPacketHandler.CHANNELS.get(channelId);
    }

    public void onRespawn() {

    }

    public void onPlayerLogin(String name) {
        UUID uuid;
        do {
            uuid = UUID.randomUUID();
        } while (this.server.getPlayerByUuid(uuid) != null);

        UUID finalUuid = uuid;

        if (this.server.getPlayerByUuid(finalUuid) != null) {
            this.connection.disconnect("Player " + name + " is already in the server.");
            return;
        }

        if (this.server.getPlayerCount() >= this.server.getMaxPlayers()) {
            this.connection.disconnect("The server is full.");
            return;
        }

        ServerPlayer player = new ServerPlayer(EntityTypes.PLAYER, this.server.getWorld(), finalUuid, name, this.connection);
        this.connection.setPlayer(player);

        Connection.LOGGER.info(name + " joined the server.");

        this.connection.send(new S2CLoginAcceptedPacket(finalUuid), PacketResult.onEither(() -> {
            this.connection.moveToInGame();
            this.connection.setHandler(new InGameServerPacketHandler(this.server, player, this.connection));

            this.server.placePlayer(player);

            BlockPos spawnPoint = UltracraftServer.invokeAndWait(() -> this.server.getWorld().getSpawnPoint());
            System.out.println("spawnPoint = " + spawnPoint);

            player.spawn(spawnPoint.vec().d().add(0.5, 0, 0.5), this.connection);
        }), true);
    }
}

package com.ultreon.craft.network.server;

import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.NetworkChannel;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.api.packet.ModPacket;
import com.ultreon.craft.network.api.packet.ModPacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.player.ServerPlayer;
import com.ultreon.libs.commons.v0.Identifier;
import net.fabricmc.api.EnvType;

import java.util.HashMap;
import java.util.Map;

public class InGameServerPacketHandler implements ServerPacketHandler {
    private static final Map<Identifier, NetworkChannel> CHANNEL = new HashMap<>();
    private final UltracraftServer server;
    private final ServerPlayer player;
    private final Connection connection;
    private final PacketContext context;

    public InGameServerPacketHandler(UltracraftServer server, ServerPlayer player, Connection connection) {
        this.server = server;
        this.player = player;
        this.connection = connection;
        this.context = new PacketContext(player, connection, EnvType.SERVER);
    }

    public static NetworkChannel registerChannel(Identifier id) {
        NetworkChannel channel = NetworkChannel.create(id);
        InGameServerPacketHandler.CHANNEL.put(id, channel);
        return channel;
    }

    @Override
    public PacketDestination destination() {
        return null;
    }

    @Override
    public void onDisconnect(String message) {

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
    public boolean isAcceptingPackets() {
        return this.connection.isConnected();
    }

    public void onModPacket(NetworkChannel channel, ModPacket<?> packet) {
        packet.handlePacket(() -> new ModPacketContext(channel, this.player, this.connection, EnvType.SERVER));
    }

    public NetworkChannel getChannel(Identifier channelId) {
        return InGameServerPacketHandler.CHANNEL.get(channelId);
    }

    public void onRespawn() {
        UltracraftServer.LOGGER.debug("Respawning player: " + this.player);
        this.server.submit(this.player::respawn);
    }

    public UltracraftServer getServer() {
        return this.server;
    }

    public void onDisconnected(String message) {
        this.server.onDisconnected(this.player, message);
    }

    public void onPlayerMove(ServerPlayer player, double dx, double dy, double dz) {
        this.server.submit(() -> player.move(dx, dy, dz));
    }
}

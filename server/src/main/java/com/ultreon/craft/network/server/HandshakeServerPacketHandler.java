package com.ultreon.craft.network.server;

import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.NetworkChannel;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.PacketResult;
import com.ultreon.craft.network.api.packet.ModPacket;
import com.ultreon.craft.network.api.packet.ModPacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.packets.handshake.S2CAcknowledgePacket;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.server.dedicated.DedicatedServer;
import com.ultreon.craft.util.AES256;
import com.ultreon.libs.commons.v0.Identifier;
import net.fabricmc.api.EnvType;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class HandshakeServerPacketHandler implements ServerPacketHandler {
    private static final Map<Identifier, NetworkChannel> CHANNELS = new HashMap<>();
    private final DedicatedServer server;
    private final Connection connection;
    private final PacketContext context;

    public HandshakeServerPacketHandler(DedicatedServer server, Connection connection) {
        this.server = server;
        this.connection = connection;
        this.context = new PacketContext(null, connection, EnvType.SERVER);
    }

    public static NetworkChannel registerChannel(Identifier id) {
        NetworkChannel channel = NetworkChannel.create(id);
        HandshakeServerPacketHandler.CHANNELS.put(id, channel);
        return channel;
    }

    @Override
    public void onDisconnect(String message) {
        UltracraftServer.LOGGER.info("Got disconnected: " + message);
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
        return HandshakeServerPacketHandler.CHANNELS.get(channelId);
    }

    public void onRespawn() {

    }

    public void onHello(byte[] encryptedKey) {
        try {
            if (!new String(AES256.decrypt(encryptedKey, this.server.getSecretKey())).equals(this.server.getPassword())) {
                this.connection.close();
            }
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException |
                 IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }

        this.connection.send(new S2CAcknowledgePacket());
    }
}

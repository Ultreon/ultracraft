package com.ultreon.craft.client.network;

import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.api.Network;
import com.ultreon.craft.network.api.PacketRegisterContext;
import com.ultreon.craft.network.api.packet.ModPacket;
import com.ultreon.craft.network.api.packet.ServerEndpoint;
import com.ultreon.craft.network.packets.ingame.C2SModPacket;

public abstract class ClientNetwork extends Network {
    protected ClientNetwork(String modId, String channelName) {
        super(modId, channelName);
    }

    @Override
    protected void registerPackets(PacketRegisterContext ctx) {

    }

    @Override
    public <T extends ModPacket<T> & ServerEndpoint> void sendToServer(T packet) {
        Connection connection = UltracraftClient.get().connection;
        if (connection != null) {
            connection.send(new C2SModPacket(this.channel, packet));
        }
    }

}

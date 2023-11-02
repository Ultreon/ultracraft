package com.ultreon.craft.network.packets.handshake;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.HandshakeClientPacketHandler;
import com.ultreon.craft.network.client.LoginClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;

public class S2CAcknowledgePacket extends Packet<HandshakeClientPacketHandler> {

    public S2CAcknowledgePacket() {

    }

    public S2CAcknowledgePacket(PacketBuffer buffer) {

    }

    @Override
    public void toBytes(PacketBuffer buffer) {

    }

    @Override
    public void handle(PacketContext ctx, HandshakeClientPacketHandler handler) {
        handler.onAcknowledge();
    }
}

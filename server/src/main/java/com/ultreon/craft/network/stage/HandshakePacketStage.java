package com.ultreon.craft.network.stage;

import com.ultreon.craft.network.packets.handshake.C2SHelloPacket;
import com.ultreon.craft.network.packets.handshake.S2CAcknowledgePacket;
import com.ultreon.craft.network.packets.C2SDisconnectPacket;
import com.ultreon.craft.network.packets.S2CDisconnectPacket;

public class HandshakePacketStage extends PacketStage {
    @SuppressWarnings("unchecked")
    @Override
    public void registerPackets() {
        this.addServerBound(C2SHelloPacket::new);
        this.addClientBound(S2CAcknowledgePacket::new);
    }
}

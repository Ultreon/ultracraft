package com.ultreon.craft.network.stage;

import com.ultreon.craft.network.packets.c2s.C2SLoginPacket;
import com.ultreon.craft.network.packets.s2c.S2CDisconnectPacket;
import com.ultreon.craft.network.packets.s2c.S2CLoginAcceptedPacket;

public class LoginPacketStage extends PacketStage {
    @Override
    public void registerPackets() {
        this.addServerBound(C2SLoginPacket::new);
        this.addClientBound(S2CLoginAcceptedPacket::new);
        this.addClientBound(S2CDisconnectPacket::new);
    }
}

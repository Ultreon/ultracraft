package com.ultreon.craft.network.stage;

import com.ultreon.craft.network.packets.C2SDisconnectPacket;
import com.ultreon.craft.network.packets.login.C2SLoginPacket;
import com.ultreon.craft.network.packets.S2CDisconnectPacket;
import com.ultreon.craft.network.packets.login.S2CLoginAcceptedPacket;

public class LoginPacketStage extends PacketStage {
    @Override
    @SuppressWarnings("unchecked")
    public void registerPackets() {
        this.addServerBound(C2SLoginPacket::new);
        this.addClientBound(S2CLoginAcceptedPacket::new);
    }
}

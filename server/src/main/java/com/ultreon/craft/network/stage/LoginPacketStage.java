package com.ultreon.craft.network.stage;

import com.ultreon.craft.network.packets.c2s.C2SDisconnectPacket;
import com.ultreon.craft.network.packets.c2s.C2SKeepAlivePacket;
import com.ultreon.craft.network.packets.c2s.C2SLoginPacket;
import com.ultreon.craft.network.packets.c2s.C2SPingPacket;
import com.ultreon.craft.network.packets.s2c.S2CDisconnectPacket;
import com.ultreon.craft.network.packets.s2c.S2CKeepAlivePacket;
import com.ultreon.craft.network.packets.s2c.S2CLoginAcceptedPacket;
import com.ultreon.craft.network.packets.s2c.S2CPingPacket;

public class LoginPacketStage extends PacketStage {
    @Override
    @SuppressWarnings("unchecked")
    public void registerPackets() {
        this.addServerBound(C2SDisconnectPacket::new);
        this.addClientBound(S2CDisconnectPacket::new);
        this.addServerBound(C2SPingPacket::new);
        this.addClientBound(S2CPingPacket::new);
        this.addServerBound(C2SKeepAlivePacket::new);
        this.addClientBound(S2CKeepAlivePacket::new);

        this.addServerBound(C2SLoginPacket::new);
        this.addClientBound(S2CLoginAcceptedPacket::new);
    }
}

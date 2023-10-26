package com.ultreon.craft.network.stage;

import com.ultreon.craft.network.packets.c2s.C2SDisconnectPacket;
import com.ultreon.craft.network.packets.c2s.C2SModPacket;
import com.ultreon.craft.network.packets.c2s.C2SPlayerMovePacket;
import com.ultreon.craft.network.packets.c2s.C2SRespawnPacket;
import com.ultreon.craft.network.packets.s2c.*;

public class InGamePacketStage extends PacketStage {
    @Override
    public void registerPackets() {
        this.addServerBound(C2SModPacket::new);
        this.addServerBound(C2SRespawnPacket::new);
        this.addServerBound(C2SDisconnectPacket::new);
        this.addServerBound(C2SPlayerMovePacket::new);

        this.addClientBound(S2CModPacket::new);
        this.addClientBound(S2CDisconnectPacket::new);
        this.addClientBound(S2CChunkStartPacket::new);
        this.addClientBound(S2CChunkPartPacket::new);
        this.addClientBound(S2CChunkFinishPacket::new);
        this.addClientBound(S2CRespawnPacket::new);
        this.addClientBound(S2CPlayerHealthPacket::new);
        this.addClientBound(S2CPlayerSetPosPacket::new);
    }
}

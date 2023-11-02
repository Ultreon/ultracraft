package com.ultreon.craft.network.stage;

import com.ultreon.craft.network.packets.C2SDisconnectPacket;
import com.ultreon.craft.network.packets.S2CDisconnectPacket;
import com.ultreon.craft.network.packets.ingame.*;

public class InGamePacketStage extends PacketStage {
    @SuppressWarnings("unchecked")
    @Override
    public void registerPackets() {
        this.addServerBound(C2SModPacket::new);
        this.addServerBound(C2SRespawnPacket::new);
        this.addServerBound(C2SPlayerMovePacket::new);
        this.addServerBound(C2SChunkStatusPacket::new);

        this.addClientBound(S2CModPacket::new);
        this.addClientBound(S2CChunkDataPacket::new);
        this.addClientBound(S2CChunkCancelPacket::new);
        this.addClientBound(S2CRespawnPacket::new);
        this.addClientBound(S2CPlayerHealthPacket::new);
        this.addClientBound(S2CPlayerSetPosPacket::new);
        this.addClientBound(S2CPlayerPositionPacket::new);
    }
}

package com.ultreon.craft.network.stage;

import com.ultreon.craft.network.packets.c2s.*;
import com.ultreon.craft.network.packets.s2c.*;

public class InGamePacketStage extends PacketStage {
    @SuppressWarnings("unchecked")
    @Override
    public void registerPackets() {
        this.addServerBound(C2SDisconnectPacket::new);
        this.addClientBound(S2CDisconnectPacket::new);

        this.addServerBound(C2SKeepAlivePacket::new);
        this.addServerBound(C2SModPacket::new);
        this.addServerBound(C2SRespawnPacket::new);
        this.addServerBound(C2SPlayerMovePacket::new);
        this.addServerBound(C2SChunkStatusPacket::new);
        this.addServerBound(C2SMenuTakeItemPacket::new);
        this.addServerBound(C2SBlockBreakingPacket::new);
        this.addServerBound(C2SBlockBreakPacket::new);
        this.addServerBound(C2SHotbarIndexPacket::new);

        this.addClientBound(S2CKeepAlivePacket::new);
        this.addClientBound(S2CModPacket::new);
        this.addClientBound(S2CChunkDataPacket::new);
        this.addClientBound(S2CChunkCancelPacket::new);
        this.addClientBound(S2CRespawnPacket::new);
        this.addClientBound(S2CPlayerHealthPacket::new);
        this.addClientBound(S2CPlayerSetPosPacket::new);
        this.addClientBound(S2CPlayerPositionPacket::new);
        this.addClientBound(S2CAddPlayerPacket::new);
        this.addClientBound(S2CRemovePlayerPacket::new);
        this.addClientBound(S2CInventoryItemChanged::new);
        this.addClientBound(S2CMenuItemChanged::new);
        this.addClientBound(S2CMenuCursorPacket::new);
        this.addClientBound(S2CBlockSetPacket::new);
    }
}

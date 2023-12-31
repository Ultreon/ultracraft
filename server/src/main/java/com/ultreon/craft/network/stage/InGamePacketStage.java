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
        this.addServerBound(C2SPingPacket::new);
        this.addServerBound(C2SModPacket::new);
        this.addServerBound(C2SRespawnPacket::new);
        this.addServerBound(C2SPlayerMovePacket::new);
        this.addServerBound(C2SChunkStatusPacket::new);
        this.addServerBound(C2SMenuTakeItemPacket::new);
        this.addServerBound(C2SBlockBreakingPacket::new);
        this.addServerBound(C2SBlockBreakPacket::new);
        this.addServerBound(C2SHotbarIndexPacket::new);
        this.addServerBound(C2SItemUsePacket::new);
        this.addServerBound(C2SCloseContainerMenuPacket::new);
        this.addServerBound(C2SOpenInventoryPacket::new);
        this.addServerBound(C2SChatPacket::new);
        this.addServerBound(C2SCommandPacket::new);
        this.addServerBound(C2SRequestTabComplete::new);
        this.addServerBound(C2SAbilitiesPacket::new);

        this.addClientBound(S2CKeepAlivePacket::new);
        this.addClientBound(S2CPingPacket::new);
        this.addClientBound(S2CModPacket::new);
        this.addClientBound(S2CChunkDataPacket::new);
        this.addClientBound(S2CChunkCancelPacket::new);
        this.addClientBound(S2CRespawnPacket::new);
        this.addClientBound(S2CPlayerHealthPacket::new);
        this.addClientBound(S2CPlayerSetPosPacket::new);
        this.addClientBound(S2CPlayerPositionPacket::new);
        this.addClientBound(S2CPlaySoundPacket::new);
        this.addClientBound(S2CAddPlayerPacket::new);
        this.addClientBound(S2CRemovePlayerPacket::new);
        this.addClientBound(S2CInventoryItemChangedPacket::new);
        this.addClientBound(S2CMenuItemChanged::new);
        this.addClientBound(S2CMenuCursorPacket::new);
        this.addClientBound(S2CBlockSetPacket::new);
        this.addClientBound(S2COpenContainerMenuPacket::new);
        this.addClientBound(S2CCloseContainerMenuPacket::new);
        this.addClientBound(S2CChatPacket::new);
        this.addClientBound(S2CCommandSyncPacket::new);
        this.addClientBound(S2CTabCompletePacket::new);
        this.addClientBound(S2CAbilitiesPacket::new);
        this.addClientBound(S2CPlayerHurtPacket::new);
        this.addClientBound(S2CGamemodePacket::new);
    }
}

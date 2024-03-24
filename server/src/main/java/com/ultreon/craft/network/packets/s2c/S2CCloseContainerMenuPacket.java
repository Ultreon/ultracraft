package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;

public class S2CCloseContainerMenuPacket extends Packet<InGameServerPacketHandler> {
    public S2CCloseContainerMenuPacket() {
        super();
    }

    public S2CCloseContainerMenuPacket(PacketBuffer buffer) {

    }

    @Override
    public void toBytes(PacketBuffer buffer) {

    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onCloseContainerMenu();
    }
}

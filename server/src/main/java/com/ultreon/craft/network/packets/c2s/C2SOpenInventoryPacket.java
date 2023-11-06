package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;

public class C2SOpenInventoryPacket extends Packet<InGameServerPacketHandler> {
    public C2SOpenInventoryPacket() {
        super();
    }

    public C2SOpenInventoryPacket(PacketBuffer buffer) {

    }

    @Override
    public void toBytes(PacketBuffer buffer) {

    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onOpenInventory();
    }
}

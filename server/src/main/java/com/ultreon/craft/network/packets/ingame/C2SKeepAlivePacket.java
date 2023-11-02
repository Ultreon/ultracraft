package com.ultreon.craft.network.packets.ingame;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.PacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;

public class C2SKeepAlivePacket<T extends PacketHandler> extends Packet<T> {
    public C2SKeepAlivePacket() {

    }

    public C2SKeepAlivePacket(PacketBuffer buffer) {

    }

    @Override
    public void toBytes(PacketBuffer buffer) {

    }

    @Override
    public void handle(PacketContext ctx, PacketHandler handler) {

    }
}

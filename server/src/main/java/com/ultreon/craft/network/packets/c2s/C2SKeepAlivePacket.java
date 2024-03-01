package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.ServerPacketHandler;

public class C2SKeepAlivePacket extends Packet<ServerPacketHandler> {
    public C2SKeepAlivePacket() {

    }

    public C2SKeepAlivePacket(PacketBuffer buffer) {

    }

    @Override
    public void toBytes(PacketBuffer buffer) {

    }

    @Override
    public void handle(PacketContext ctx, ServerPacketHandler handler) {
        handler.onKeepAlive();
    }
}

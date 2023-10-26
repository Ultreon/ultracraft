package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;

public class C2SRespawnPacket extends Packet<InGameServerPacketHandler> {
    public C2SRespawnPacket() {

    }

    public C2SRespawnPacket(PacketBuffer ignoredBuffer) {

    }

    @Override
    public void toBytes(PacketBuffer buffer) {

    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler listener) {
        listener.onRespawn();
    }
}

package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;

public class C2SPingPacket extends Packet<InGameServerPacketHandler> {
    private final long time;

    public C2SPingPacket() {
        this.time = System.currentTimeMillis();
    }

    public C2SPingPacket(PacketBuffer buffer) {
        this.time = buffer.readLong();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeLong(this.time);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onPing(this.time);
    }
}

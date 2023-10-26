package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;

public class C2SDisconnectPacket extends Packet<InGameServerPacketHandler> {
    private final String message;

    public C2SDisconnectPacket(String message) {
        this.message = message;
    }

    public C2SDisconnectPacket(PacketBuffer buffer) {
        this.message = buffer.readString(300);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeString(this.message, 300);
    }

    @Override
    public void handle(PacketContext packetContext, InGameServerPacketHandler listener) {
        listener.onDisconnected(this.message);
    }
}

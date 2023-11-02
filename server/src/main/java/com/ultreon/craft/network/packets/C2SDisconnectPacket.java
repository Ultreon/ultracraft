package com.ultreon.craft.network.packets;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.PacketHandler;
import com.ultreon.craft.network.packets.Packet;

public class C2SDisconnectPacket<T extends PacketHandler> extends Packet<T> {
    private final String message;

    public C2SDisconnectPacket(String message) {
        this.message = message;
    }

    public C2SDisconnectPacket(PacketBuffer buffer) {
        this.message = buffer.readUTF(300);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeUTF(this.message, 300);
    }

    @Override
    public void handle(PacketContext packetContext, T handler) {
        handler.onDisconnect(this.message);
    }
}

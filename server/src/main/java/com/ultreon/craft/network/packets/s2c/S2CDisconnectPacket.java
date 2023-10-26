package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.packets.Packet;

public class S2CDisconnectPacket extends Packet<InGameClientPacketHandler> {
    private String message;

    public S2CDisconnectPacket(String message) {
        this.message = message;
    }

    public S2CDisconnectPacket(PacketBuffer buffer) {
        this.message = buffer.readString(300);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeString(this.message, 300);
    }

    @Override
    public void handle(PacketContext packetContext, InGameClientPacketHandler listener) {
        listener.onDisconnect(this.message);
    }
}

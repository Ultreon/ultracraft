package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.ServerPacketHandler;

public class C2SDisconnectPacket<T extends ServerPacketHandler> extends Packet<T> {
    private final String message;

    public C2SDisconnectPacket(String message) {
        this.message = message;
    }

    public C2SDisconnectPacket(PacketBuffer buffer) {
        this.message = buffer.readString(300);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeUTF(this.message, 300);
    }

    @Override
    public void handle(PacketContext packetContext, T handler) {
        CommonConstants.LOGGER.info("Client disconnected: {}", this.message);
        handler.onDisconnect(this.message);
    }
}

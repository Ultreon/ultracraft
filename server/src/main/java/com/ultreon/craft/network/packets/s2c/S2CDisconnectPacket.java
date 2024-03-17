package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.CommonConstants;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;

public class S2CDisconnectPacket<T extends ClientPacketHandler> extends Packet<T> {
    private final String message;

    public S2CDisconnectPacket(String message) {
        this.message = message;
    }

    public S2CDisconnectPacket(PacketBuffer buffer) {
        this.message = buffer.readString(300);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeUTF(this.message, 300);
    }

    @Override
    public void handle(PacketContext packetContext, T handler) {
        CommonConstants.LOGGER.info("Server disconnected: {}", this.message);

        handler.onDisconnect(this.message);
    }
}

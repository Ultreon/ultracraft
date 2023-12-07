package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.ClientPacketHandler;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;

public class S2CPingPacket extends Packet<ClientPacketHandler> {
    private final long serverTime;
    private final long time;

    public S2CPingPacket(long time) {
        this.serverTime = System.currentTimeMillis();
        this.time = time;
    }

    public S2CPingPacket(PacketBuffer buffer) {
        this.serverTime = buffer.readLong();
        this.time = buffer.readLong();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeLong(this.serverTime);
        buffer.writeLong(this.time);
    }

    @Override
    public void handle(PacketContext ctx, ClientPacketHandler handler) {
        if (handler instanceof InGameClientPacketHandler inGameHandler) {
            inGameHandler.onPing(this.serverTime, this.time);
        }
    }
}

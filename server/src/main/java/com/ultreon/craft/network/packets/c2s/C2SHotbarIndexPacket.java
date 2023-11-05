package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;

public class C2SHotbarIndexPacket extends Packet<InGameServerPacketHandler> {
    private final int hotbarIdx;

    public C2SHotbarIndexPacket(int hotbarIdx) {
        this.hotbarIdx = hotbarIdx;
    }

    public C2SHotbarIndexPacket(PacketBuffer buffer) {
        this.hotbarIdx = buffer.readByte();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeByte(this.hotbarIdx);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onHotbarIndex(this.hotbarIdx);
    }
}

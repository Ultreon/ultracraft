package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;

public class C2SMenuTakeItemPacket extends Packet<InGameServerPacketHandler> {
    private final int index;
    private final boolean split;

    public C2SMenuTakeItemPacket(int index, boolean split) {
        this.index = index;
        this.split = split;
    }

    public C2SMenuTakeItemPacket(PacketBuffer buffer) {
        this.index = buffer.readInt();
        this.split = buffer.readBoolean();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(this.index);
        buffer.writeBoolean(this.split);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onTakeItem(this.index, this.split);
    }
}

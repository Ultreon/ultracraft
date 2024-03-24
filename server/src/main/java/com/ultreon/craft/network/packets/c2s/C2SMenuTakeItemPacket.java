package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;

public class C2SMenuTakeItemPacket extends Packet<InGameServerPacketHandler> {
    private final int index;
    private final boolean rightClick;

    public C2SMenuTakeItemPacket(int index, boolean rightClick) {
        this.index = index;
        this.rightClick = rightClick;
    }

    public C2SMenuTakeItemPacket(PacketBuffer buffer) {
        this.index = buffer.readInt();
        this.rightClick = buffer.readBoolean();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeInt(this.index);
        buffer.writeBoolean(this.rightClick);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onTakeItem(this.index, this.rightClick);
    }
}

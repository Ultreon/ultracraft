package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;

public class C2SPlayerMovePacket extends Packet<InGameServerPacketHandler> {
    private final double x;
    private final double y;
    private final double z;

    public C2SPlayerMovePacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public C2SPlayerMovePacket(PacketBuffer buffer) {
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onPlayerMove(ctx.requirePlayer(), this.x, this.y, this.z);
    }
}

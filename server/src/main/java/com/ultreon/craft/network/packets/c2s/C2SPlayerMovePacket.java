package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;

public class C2SPlayerMovePacket extends Packet<InGameServerPacketHandler> {
    private final double dx;
    private final double dy;
    private final double dz;

    public C2SPlayerMovePacket(double dx, double dy, double dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public C2SPlayerMovePacket(PacketBuffer buffer) {
        this.dx = buffer.readDouble();
        this.dy = buffer.readDouble();
        this.dz = buffer.readDouble();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeDouble(this.dx);
        buffer.writeDouble(this.dy);
        buffer.writeDouble(this.dz);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onPlayerMove(ctx.requirePlayer(), this.dx, this.dy, this.dz);
    }
}

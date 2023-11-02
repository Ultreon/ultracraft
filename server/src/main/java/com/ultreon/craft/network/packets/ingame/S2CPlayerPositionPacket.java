package com.ultreon.craft.network.packets.ingame;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.libs.commons.v0.vector.Vec3d;

import java.util.List;

public class S2CPlayerPositionPacket extends Packet<InGameClientPacketHandler> {
    private final List<Vec3d> list;

    public S2CPlayerPositionPacket(List<Vec3d> list) {
        this.list = list;
    }

    public S2CPlayerPositionPacket(PacketBuffer buffer) {
        this.list = buffer.readList(PacketBuffer::readVec3d);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeList(this.list, PacketBuffer::writeVec3d);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlayerPositions(ctx, this.list);
    }
}

package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.libs.commons.v0.vector.Vec3d;

public class S2CRespawnPacket extends Packet<InGameClientPacketHandler> {
    private final Vec3d pos;

    public S2CRespawnPacket(Vec3d pos) {
        this.pos = pos;
    }

    public S2CRespawnPacket(PacketBuffer buffer) {
        this.pos = buffer.readVec3d();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeVec3d(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onRespawn(this.pos);
    }
}

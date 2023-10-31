package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.libs.commons.v0.vector.Vec3d;

public class S2CPlayerSetPosPacket extends Packet<InGameClientPacketHandler> {
    private final Vec3d pos;

    public S2CPlayerSetPosPacket(double x, double y, double z) {
        this(new Vec3d(x, y, z));
    }

    public S2CPlayerSetPosPacket(Vec3d pos) {
        this.pos = pos;
    }

    public S2CPlayerSetPosPacket(PacketBuffer buffer) {
        this.pos = buffer.readVec3d();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeVec3d(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onPlayerSetPos(this.pos);
    }
}

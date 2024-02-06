package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;

public class S2CTimePacket extends Packet<InGameClientPacketHandler> {
    private final Operation operation;
    private final int time;

    public S2CTimePacket(Operation operation, int time) {
        this.operation = operation;
        this.time = time;
    }

    public S2CTimePacket(PacketBuffer buffer) {
        this.operation = Operation.values()[buffer.readVarInt()];
        this.time = buffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeVarInt(operation.ordinal());
        buffer.writeInt(time);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onTimeChange(ctx, operation, time);
    }

    public enum Operation {
        SET,
        ADD,
        SUB
    }
}

package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;
import com.ultreon.craft.world.BlockPos;

public class C2SBlockBreakingPacket extends Packet<InGameServerPacketHandler> {
    private final BlockPos pos;
    private final BlockStatus status;

    public C2SBlockBreakingPacket(BlockPos pos, BlockStatus status) {
        this.status = status;
        this.pos = pos;
    }

    public C2SBlockBreakingPacket(PacketBuffer buffer) {
        this.status = BlockStatus.values()[buffer.readByte()];
        this.pos = buffer.readBlockPos();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeByte(this.status.ordinal());
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onBlockBreaking(this.pos, this.status);
    }

    public enum BlockStatus {
        START,
        CONTINUE,
        STOP,
    }
}

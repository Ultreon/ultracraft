package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;
import com.ultreon.craft.world.BlockPos;

public class C2SBlockBreakPacket extends Packet<InGameServerPacketHandler> {
    private final BlockPos pos;

    public C2SBlockBreakPacket(BlockPos pos) {
        this.pos = pos;
    }

    public C2SBlockBreakPacket(PacketBuffer buffer) {
        this.pos = buffer.readBlockPos();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onBlockBroken(this.pos);
    }
}

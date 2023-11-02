package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.libs.commons.v0.Identifier;

public class S2CBlockSetPacket extends Packet<InGameClientPacketHandler> {
    private final BlockPos pos;
    private final Identifier block;

    public S2CBlockSetPacket(BlockPos pos, Identifier block) {
        this.pos = pos;
        this.block = block;
    }

    public S2CBlockSetPacket(PacketBuffer buffer) {
        this.pos = buffer.readBlockPos();
        this.block = buffer.readId();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeId(this.block);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onBlockSet(this.pos, this.block);
    }
}

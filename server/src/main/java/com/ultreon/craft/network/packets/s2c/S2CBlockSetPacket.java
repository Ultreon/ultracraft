package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.world.BlockPos;

public class S2CBlockSetPacket extends Packet<InGameClientPacketHandler> {
    private final BlockPos pos;
    private final BlockMetadata blockMeta;

    public S2CBlockSetPacket(BlockPos pos, BlockMetadata blockMeta) {
        this.pos = pos;
        this.blockMeta = blockMeta;
    }

    public S2CBlockSetPacket(PacketBuffer buffer) {
        this.pos = buffer.readBlockPos();
        this.blockMeta = buffer.readBlockMeta();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeBlockMeta(this.blockMeta);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onBlockSet(this.pos, this.blockMeta);
    }
}

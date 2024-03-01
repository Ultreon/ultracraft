package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.world.BlockPos;

public class S2CBlockSetPacket extends Packet<InGameClientPacketHandler> {
    private final BlockPos pos;
    private final int blockId;

    public S2CBlockSetPacket(BlockPos pos, int blockId) {
        this.pos = pos;
        this.blockId = blockId;
    }

    public S2CBlockSetPacket(PacketBuffer buffer) {
        this.pos = buffer.readBlockPos();
        this.blockId = buffer.readVarInt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeVarInt(this.blockId);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onBlockSet(this.pos, Registries.BLOCK.get(this.blockId));
    }
}

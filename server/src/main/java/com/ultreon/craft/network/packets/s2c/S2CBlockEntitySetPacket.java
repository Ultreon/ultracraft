package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.world.BlockPos;

public class S2CBlockEntitySetPacket extends Packet<InGameClientPacketHandler> {
    private final BlockPos pos;
    private final int blockEntityId;

    public S2CBlockEntitySetPacket(BlockPos pos, int blockEntityId) {
        this.pos = pos;
        this.blockEntityId = blockEntityId;
    }

    public S2CBlockEntitySetPacket(PacketBuffer buffer) {
        this.pos = buffer.readBlockPos();
        this.blockEntityId = buffer.readVarInt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeVarInt(this.blockEntityId);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onBlockEntitySet(this.pos, Registries.BLOCK_ENTITY_TYPE.byId(this.blockEntityId));
    }
}

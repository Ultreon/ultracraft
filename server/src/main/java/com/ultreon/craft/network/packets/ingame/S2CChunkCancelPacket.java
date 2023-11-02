package com.ultreon.craft.network.packets.ingame;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.world.ChunkPos;

public class S2CChunkCancelPacket extends Packet<InGameClientPacketHandler> {
    private final ChunkPos pos;

    public S2CChunkCancelPacket(ChunkPos pos) {
        this.pos = pos;
    }

    public S2CChunkCancelPacket(PacketBuffer buffer) {
        this.pos = buffer.readChunkPos();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeChunkPos(this.pos);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onChunkCancel(this.pos);
    }
}

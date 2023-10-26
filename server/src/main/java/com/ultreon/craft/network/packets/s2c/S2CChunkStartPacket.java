package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.world.ChunkPos;

public class S2CChunkStartPacket extends Packet<InGameClientPacketHandler> {
    private final ChunkPos pos;
    private final int dataLength;

    public S2CChunkStartPacket(ChunkPos pos, int dataLength) {
        this.pos = pos;
        this.dataLength = dataLength;
    }

    public S2CChunkStartPacket(PacketBuffer buffer) {
        this.pos = buffer.readChunkPos();
        this.dataLength = buffer.readUnsignedShort();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeChunkPos(this.pos);
        buffer.writeShort(this.dataLength);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler listener) {
        listener.onChunkStart(this.pos, this.dataLength);
    }
}

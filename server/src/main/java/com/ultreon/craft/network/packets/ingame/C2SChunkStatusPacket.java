package com.ultreon.craft.network.packets.ingame;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;

public class C2SChunkStatusPacket extends Packet<InGameServerPacketHandler> {
    private final Chunk.Status status;
    private final ChunkPos pos;

    public C2SChunkStatusPacket(ChunkPos pos, Chunk.Status status) {
        this.pos = pos;
        this.status = status;
    }

    public C2SChunkStatusPacket(PacketBuffer buffer) {
        this.pos = buffer.readChunkPos();
        this.status = Chunk.Status.values()[buffer.readUnsignedShort()];
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeChunkPos(this.pos);
        buffer.writeShort(this.status.ordinal());
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        assert ctx.getPlayer() != null;
        handler.onChunkStatus(ctx.getPlayer(), this.pos, this.status);
    }
}

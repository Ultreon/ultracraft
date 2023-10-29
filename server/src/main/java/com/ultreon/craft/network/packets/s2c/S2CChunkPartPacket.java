package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.world.ChunkPos;
import org.jetbrains.annotations.NotNull;

public class S2CChunkPartPacket extends Packet<InGameClientPacketHandler> {
    private final ChunkPos pos;
    private final byte @NotNull [] hash;
    private final byte[] partialData;
    public static final int BUFFER_SIZE = 65536;

    public S2CChunkPartPacket(ChunkPos pos, byte @NotNull [] hash, byte[] partialData) {
        this.pos = pos;
        this.hash = hash;
        this.partialData = partialData;
    }

    public S2CChunkPartPacket(PacketBuffer buffer) {
        this.pos = buffer.readChunkPos();
        this.hash = buffer.readByteArray(16);
        this.partialData = buffer.readByteArray(S2CChunkPartPacket.BUFFER_SIZE);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeChunkPos(this.pos);
        buffer.writeByteArray(this.hash, 16);
        buffer.writeByteArray(this.partialData, S2CChunkPartPacket.BUFFER_SIZE);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler listener) {
        listener.onChunkStart(this.pos, this.hash, this.partialData.length);
        listener.onChunkPart(this.pos, this.partialData);
        listener.onChunkFinish(this.pos);
    }
}

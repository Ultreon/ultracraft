package com.ultreon.craft.network.packets.ingame;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.ServerWorld;
import com.ultreon.craft.world.World;
import com.ultreon.data.types.MapType;

import java.util.List;
import java.util.function.Function;

public class S2CChunkDataPacket extends Packet<InGameClientPacketHandler> {
    private final ChunkPos pos;
    private final short[] palette;
    private final List<Block> data;
    private Function<PacketBuffer, Chunk> decoder = null;
    public static final int MAX_SIZE = 1048576;

    public S2CChunkDataPacket(PacketBuffer buffer) {
        this.pos = buffer.readChunkPos();
        this.palette = buffer.readShortArray();
        this.data = buffer.readList(buf -> Chunk.decodeBlock(buf.readUbo()));
    }

    public S2CChunkDataPacket(ChunkPos pos, short[] palette, List<Block> data) {
        this.pos = pos;
        this.palette = palette;
        this.data = data;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeChunkPos(this.pos);
        buffer.writeShortArray(this.palette);
        buffer.writeList(this.data, (buf, block) -> buf.writeUbo(block.save()));
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onChunkData(this.pos, this.palette, this.data);
    }
}

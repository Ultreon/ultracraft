package com.ultreon.craft.network.packets.c2s;

import com.ultreon.craft.block.state.BlockMetadata;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.network.server.InGameServerPacketHandler;

public class C2SPlaceBlockPacket extends Packet<InGameServerPacketHandler> {
    private final int x;
    private final int y;
    private final int z;
    private final BlockMetadata block;

    public C2SPlaceBlockPacket(PacketBuffer buffer) {
        x = buffer.readVarInt();
        y = buffer.readVarInt();
        z = buffer.readVarInt();
        block = BlockMetadata.read(buffer);
    }

    public C2SPlaceBlockPacket(int x, int y, int z, BlockMetadata block) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeVarInt(x);
        buffer.writeVarInt(y);
        buffer.writeVarInt(z);
        block.write(buffer);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onPlaceBlock(x, y, z, block);
    }
}

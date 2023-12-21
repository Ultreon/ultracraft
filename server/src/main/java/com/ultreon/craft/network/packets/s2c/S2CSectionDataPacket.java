package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.world.ChunkSection;
import com.ultreon.craft.world.SectionPos;

public class S2CSectionDataPacket extends Packet<InGameClientPacketHandler> {
    private final SectionPos pos;
    private final long[] data;
    private final Block[] palette;
    public static final int MAX_SIZE = 1048576;

    public S2CSectionDataPacket(PacketBuffer buffer) {
        this.pos = buffer.readSectionPos();
        this.data = buffer.readLongArray();
        this.palette = buffer.readArray(buf -> Registries.BLOCK.byId(buf.readVarInt()), 4096);
    }

    public S2CSectionDataPacket(SectionPos pos, ChunkSection section) {
        this.pos = pos;
        this.data = section.blocks.getData();
        this.palette = section.blocks.getPalette();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeSectionPos(this.pos);
        buffer.writeLongArray(this.data);
        buffer.writeArray(this.palette, (buf, block) -> buf.writeVarInt(Registries.BLOCK.getId(block)));
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onSectionData(this.pos, this.data, this.palette);
    }
}

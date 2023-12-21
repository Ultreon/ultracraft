package com.ultreon.craft.world;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.collection.PaletteStorage;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.registry.Registries;
import com.ultreon.data.types.MapType;

public class ChunkSection {
    public final PaletteStorage<Block> blocks = new PaletteStorage<>(4096, Blocks.AIR);

    public ChunkSection() {

    }

    public ChunkSection(long[] palette, Block[] data) {
        this.blocks.set(palette, data);
    }

    public ChunkSection(PacketBuffer buffer) {
        this.blocks.set(buffer.readLongArray(), buffer.readArray(buf -> Registries.BLOCK.byId(buf.readVarInt()), 4096));
    }

    public Block get(int idx) {
        return this.blocks.get(idx);
    }

    public void set(int index, Block block) {
        this.blocks.set(index, block);
    }

    public void dispose() {
        this.blocks.dispose();
    }

    public MapType save() {
        MapType data = new MapType();
        this.blocks.save(data, Block::save);
        return data;
    }

    public void write(PacketBuffer buffer) {
        this.blocks.write(buffer, (block, packetBuffer) -> packetBuffer.writeVarInt(Registries.BLOCK.getId(block)));
    }
}

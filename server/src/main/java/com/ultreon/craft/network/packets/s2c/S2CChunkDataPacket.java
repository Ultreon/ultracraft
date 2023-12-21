package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.collection.PaletteStorage;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.world.*;
import com.ultreon.craft.world.gen.biome.Biomes;

import static com.ultreon.craft.world.World.CHUNK_SIZE;

public class S2CChunkDataPacket extends Packet<InGameClientPacketHandler> {
    private final ChunkPos pos;
    private final PaletteStorage<Biome> biomes;

    public S2CChunkDataPacket(ChunkPos pos, ServerChunk chunk) {
        this.pos = pos;
        this.biomes = chunk.biomes;
    }

    public S2CChunkDataPacket(PacketBuffer buffer) {
        this.pos = buffer.readChunkPos();
        this.biomes = new PaletteStorage<>(buffer.readLongArray(), buffer.readArray(buf -> Registries.BIOME.byId(buf.readVarInt()), CHUNK_SIZE * CHUNK_SIZE));
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeChunkPos(this.pos);
        buffer.writeLongArray(this.biomes.getData());
        buffer.writeArray(this.biomes.getPalette(), (buf, biome) -> buf.writeVarInt(Registries.BIOME.getId(biome)));
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onChunkData(this.pos, this.biomes);
    }
}

package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.collection.FlatStorage;
import com.ultreon.craft.collection.Storage;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.gen.biome.Biomes;

public class S2CChunkDataPacket extends Packet<InGameClientPacketHandler> {
    private final ChunkPos pos;
    private final Storage<Block> storage;
    private final Storage<Biome> biomeStorage;
    public static final int MAX_SIZE = 1048576;

    public S2CChunkDataPacket(PacketBuffer buffer) {
        this.pos = buffer.readChunkPos();
        this.storage = new FlatStorage<>(buffer, buf -> Registries.BLOCK.byId(buf.readShort()));
        this.biomeStorage = new FlatStorage<>(buffer, buf -> Registries.BIOME.byId(buf.readShort()));
    }

    public S2CChunkDataPacket(ChunkPos pos, Storage<Block> storage, Storage<Biome> biomeStorage) {
        this.pos = pos;
        this.storage = storage;
        this.biomeStorage = biomeStorage;
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeChunkPos(this.pos);
        this.storage.write(buffer, (encode, block) -> encode.writeShort(Registries.BLOCK.getId(block)));
        this.biomeStorage.write(buffer, (encode, biome) -> {
            if (biome == null) {
                encode.writeShort(Registries.BIOME.getId(Biomes.VOID));
                return;
            }
            encode.writeShort(Registries.BIOME.getId(biome));
        });
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onChunkData(this.pos, this.storage, this.biomeStorage);
    }
}

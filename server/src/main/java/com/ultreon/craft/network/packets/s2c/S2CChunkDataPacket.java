package com.ultreon.craft.network.packets.s2c;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.block.entity.BlockEntity;
import com.ultreon.craft.block.entity.BlockEntityType;
import com.ultreon.craft.collection.PaletteStorage;
import com.ultreon.craft.collection.Storage;
import com.ultreon.craft.network.PacketBuffer;
import com.ultreon.craft.network.PacketContext;
import com.ultreon.craft.network.client.InGameClientPacketHandler;
import com.ultreon.craft.network.packets.Packet;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.world.Biome;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.biome.Biomes;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.*;

public class S2CChunkDataPacket extends Packet<InGameClientPacketHandler> {
    private final ChunkPos pos;
    private final Storage<Block> storage;
    private final Storage<Biome> biomeStorage;
    private final IntList blockEntityPositions = new IntArrayList();
    private final IntList blockEntities = new IntArrayList();
    public static final int MAX_SIZE = 1048576;

    public S2CChunkDataPacket(PacketBuffer buffer) {
        this.pos = buffer.readChunkPos();
        this.storage = new PaletteStorage<>(Blocks.AIR, buffer, buf -> Registries.BLOCK.get(buf.readShort()));
        this.biomeStorage = new PaletteStorage<>(Biomes.PLAINS, buffer, buf -> Registries.BIOME.get(buf.readShort()));

        int blockEntityCount = buffer.readVarInt();
        for (int i = 0; i < blockEntityCount; i++) {
            blockEntityPositions.add(buffer.readMedium());
            blockEntities.add(buffer.readVarInt());
        }
    }

    public S2CChunkDataPacket(ChunkPos pos, Storage<Block> storage, Storage<Biome> biomeStorage, Collection<BlockEntity> blockEntities) {
        this.pos = pos;
        this.storage = storage;
        this.biomeStorage = biomeStorage;

        for (BlockEntity blockEntity : blockEntities) {
            BlockPos bPos = World.localize(blockEntity.pos());
            this.blockEntityPositions.add((bPos.x() % 16) << 20 | (bPos.y() % 65536) << 4 | bPos.z() % 16);
            this.blockEntities.add(blockEntity.getType().getRawId());
        }
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeChunkPos(this.pos);
        this.storage.write(buffer, (encode, block) -> {
            if (block == null) {
                encode.writeShort(Registries.BLOCK.getRawId(Blocks.ERROR));
                return;
            }
            int rawId = Registries.BLOCK.getRawId(block);
            if (rawId == -1) {
                encode.writeShort(Registries.BLOCK.getRawId(Blocks.ERROR));
                return;
            }
            encode.writeShort(rawId);
        });
        this.biomeStorage.write(buffer, (encode, biome) -> {
            if (biome == null) {
                encode.writeShort(Registries.BIOME.getRawId(Biomes.VOID));
                return;
            }
            encode.writeShort(Registries.BIOME.getRawId(biome));
        });

        buffer.writeVarInt(this.blockEntities.size());
        for (int blockEntity : this.blockEntities) {
            buffer.writeMedium(blockEntityPositions.getInt(blockEntity));
            buffer.writeVarInt(blockEntity);
        }
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        Map<BlockPos, BlockEntityType<?>> blockEntities = new HashMap<>();
        for (Integer blockEntityPosition : this.blockEntityPositions) {
            int x = (blockEntityPosition >> 20) & 0xF;
            int y = (blockEntityPosition >> 4) & 0xFFFF;
            int z = blockEntityPosition & 0xF;
            blockEntities.put(new BlockPos(x, y, z).offset(this.pos), Registries.BLOCK_ENTITY_TYPE.get(this.blockEntities.getInt(blockEntityPosition)));
        }

        handler.onChunkData(this.pos, this.storage, this.biomeStorage, blockEntities);
    }
}

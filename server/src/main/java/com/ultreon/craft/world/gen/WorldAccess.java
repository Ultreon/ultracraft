package com.ultreon.craft.world.gen;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.ChunkAccess;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.Nullable;

public interface WorldAccess {
    Block get(int x, int y, int z);

    default Block get(BlockPos pos) {
        return get(pos.x(), pos.y(), pos.z());
    }

    default Block get(Vec3i pos) {
        return get(pos.x, pos.y, pos.z);
    }

    boolean set(int x, int y, int z, Block block);

    default boolean set(BlockPos pos, Block block) {
        return set(pos.x(), pos.y(), pos.z(), block);
    }

    default boolean set(Vec3i pos, Block block) {
        return set(pos.x, pos.y, pos.z, block);
    }

    default @Nullable ChunkAccess getChunk(int x, int y, int z) {
        return getChunk(new ChunkPos(x, y, z));
    }

    default @Nullable ChunkAccess getChunk(Vec3i pos) {
        return getChunk(new ChunkPos(pos.x, pos.y, pos.z));
    }

    @Nullable ChunkAccess getChunk(ChunkPos pos);

    default @Nullable ChunkAccess getChunkAt(BlockPos pos) {
        ChunkPos chunkPos = World.blockToChunkPos(pos.vec());
        return getChunk(chunkPos);
    }

    default @Nullable ChunkAccess getChunkAt(Vec3i pos) {
        ChunkPos chunkPos = World.blockToChunkPos(pos);
        return getChunk(chunkPos);
    }

    default @Nullable ChunkAccess getChunkAt(int x, int y, int z) {
        ChunkPos chunkPos = World.toChunkPos(x, y, z);
        return getChunk(chunkPos);
    }

    long getSeed();

    int getHighest(int x, int z);
}

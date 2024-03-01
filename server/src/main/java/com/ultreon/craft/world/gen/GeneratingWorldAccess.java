package com.ultreon.craft.world.gen;

import com.ultreon.craft.block.Block;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.world.*;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.Nullable;

import static com.ultreon.craft.world.World.CHUNK_SIZE;

public class GeneratingWorldAccess implements WorldAccess {
    private final ServerWorld world;
    private final RecordingChunk chunk;

    public GeneratingWorldAccess(ServerWorld world, RecordingChunk chunk) {
        this.world = world;
        this.chunk = chunk;
    }

    public ServerWorld getWorld() {
        return world;
    }

    @Override
    public Block get(int x, int y, int z) {
        BlockPos blockPos = World.localizeBlock(x, y, z);

        if (!chunk.isOutOfBounds(blockPos.x(), blockPos.y(), blockPos.z()))
            return chunk.get(blockPos);

        @Nullable Block block = world.recordedChangeAt(x, y, z);
        return block != null ? block : world.get(x, y, z);
    }

    @Override
    public boolean set(int x, int y, int z, Block block) {
        Vec3i chunkOffset = chunk.getOffset();
        boolean isWithinChunk = x >= chunkOffset.x && x < chunkOffset.x + CHUNK_SIZE &&
                    y >= chunkOffset.y && y < chunkOffset.y + CHUNK_SIZE &&
                    z >= chunkOffset.z && z < chunkOffset.z + CHUNK_SIZE;
        if (isWithinChunk) {
            return chunk.set(World.localizeBlock(x, y, z), block);
        }

        UltracraftServer.invoke(() -> world.recordOutOfBounds(x, y, z, block));
        return false;
    }

    @Override
    public ChunkAccess getChunk(ChunkPos pos) {
        return chunk.getPos().equals(pos) ? chunk : world.getChunk(pos);
    }

    @Override
    public long getSeed() {
        return world.getSeed();
    }

    @Override
    public int getHighest(int x, int z) {
        return world.getHighest(x, z);
    }
}

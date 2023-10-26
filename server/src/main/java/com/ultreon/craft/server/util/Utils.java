package com.ultreon.craft.server.util;

import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Utils {
    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

    public static ChunkPos chunkPosFromBlockCoords(Vec3d pos) {
        return new ChunkPos(Math.floorDiv((int) pos.x, World.CHUNK_SIZE), Math.floorDiv((int) pos.z, World.CHUNK_SIZE));
    }

    @Deprecated
    public static ChunkPos chunkPosFromBlockCoords(Vec3i pos) {
        return new ChunkPos(Math.floorDiv(pos.x, World.CHUNK_SIZE), Math.floorDiv(pos.z, World.CHUNK_SIZE));
    }

    public static ChunkPos toChunkPos(BlockPos pos) {
        return new ChunkPos(Math.floorDiv(pos.x(), World.CHUNK_SIZE), Math.floorDiv(pos.z(), World.CHUNK_SIZE));
    }

    public static int normalizeToInt(byte b) {
        return b < 0 ? (int)b + 128 : b;
    }
}

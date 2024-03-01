package com.ultreon.craft.server.util;

import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.World;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.ultreon.libs.datetime.v0.Duration;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Utils {
    public static final UUID ZEROED_UUID = new UUID(0, 0);

    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

    public static ChunkPos chunkPosFromBlockCoords(Vec3d pos) {
        return new ChunkPos(Math.floorDiv((int) pos.x, World.CHUNK_SIZE), Math.floorDiv((int) pos.y, World.CHUNK_SIZE), Math.floorDiv((int) pos.z, World.CHUNK_SIZE));
    }

    @Deprecated
    public static ChunkPos chunkPosFromBlockCoords(Vec3i pos) {
        return new ChunkPos(Math.floorDiv(pos.x, World.CHUNK_SIZE), Math.floorDiv(pos.y, World.CHUNK_SIZE), Math.floorDiv(pos.z, World.CHUNK_SIZE));
    }

    public static ChunkPos toChunkPos(BlockPos pos) {
        return new ChunkPos(Math.floorDiv(pos.x(), World.CHUNK_SIZE), Math.floorDiv(pos.y(), World.CHUNK_SIZE), Math.floorDiv(pos.z(), World.CHUNK_SIZE));
    }

    public static int normalizeToInt(byte b) {
        return b < 0 ? (int)b + 128 : b;
    }

    public static Duration parseDuration(String text) {
        try {
            String[] parts = text.split(":");
            long days = 0, hours = 0, minutes = 0, seconds = 0;

            seconds = Utils.getDurationNum(parts[parts.length - 1], true);
            minutes = Utils.getDurationNum(parts[parts.length - 2], parts.length >= 3);
            if (parts.length >= 3) hours = Utils.getDurationNum(parts[parts.length - 3], parts.length >= 4);
            if (parts.length >= 4) days = Utils.getDurationNum(parts[parts.length - 4], false);
            return Duration.ofSeconds(seconds + (minutes * 60) + (hours * 3600) + (days * 86400));
        } catch (NumberFormatException e) {
            throw new TimeFormatException("Invalid number format: " + e, e);
        }
    }

    private static long getDurationNum(String part, boolean trim) {
        if (!trim) {
            return Integer.parseInt(part);
        }
        return 0;
    }

    public static String reprChar(char c) {
        if (c == '\r') return "'\\r'";
        if (c == '\n') return "'\\n'";

        return c == '\t' ? "'\\t'" : "'" + c + "'";
    }
}

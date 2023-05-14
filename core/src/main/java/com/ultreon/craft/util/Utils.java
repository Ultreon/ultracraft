package com.ultreon.craft.util;

import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.craft.world.ChunkPos;
import com.ultreon.craft.world.World;

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

    public static ChunkPos chunkPosFromBlockCoords(Vector3 pos) {
        return new ChunkPos(Math.floorDiv((int) pos.x, World.CHUNK_SIZE), Math.floorDiv((int) pos.z, World.CHUNK_SIZE));
    }

    public static ChunkPos chunkPosFromBlockCoords(GridPoint3 pos) {
        return new ChunkPos(Math.floorDiv(pos.x, World.CHUNK_SIZE), Math.floorDiv(pos.z, World.CHUNK_SIZE));
    }
}
